#!/usr/bin/env python3
"""Generate or verify markdown TOCs between HTML markers."""

from __future__ import annotations

import argparse
import difflib
import glob
import re
import sys
from pathlib import Path

TOC_MARKER = "<!--- TOC -->"
END_MARKER = "<!--- END -->"
HEADER_RE = re.compile(r"^(#{1,6})\s+(.+?)\s*$")
FENCE_RE = re.compile(r"^\s*(`{3,}|~{3,})")


def parse_headers(content: str) -> list[tuple[int, str]]:
    """Extract markdown headers after the first END marker, excluding fenced code blocks."""
    end_index = content.find(END_MARKER)
    if end_index == -1:
        return []

    scan_region = content[end_index + len(END_MARKER) :]
    headers: list[tuple[int, str]] = []
    in_fence = False
    fence_char = ""
    fence_len = 0

    for line in scan_region.splitlines():
        fence_match = FENCE_RE.match(line)
        if fence_match:
            fence = fence_match.group(1)
            if not in_fence:
                in_fence = True
                fence_char = fence[0]
                fence_len = len(fence)
            elif fence[0] == fence_char and len(fence) >= fence_len:
                in_fence = False
            continue

        if in_fence:
            continue

        match = HEADER_RE.match(line)
        if not match:
            continue

        level = len(match.group(1))
        text = re.sub(r"\s+#+\s*$", "", match.group(2)).strip()
        if text:
            headers.append((level, text))

    return headers


def _slugify(text: str) -> str:
    """Generate a markdown anchor similar to GitHub style."""
    anchor = text.lower().strip()
    anchor = re.sub(r"[^\w\s-]", "", anchor)
    anchor = re.sub(r"\s+", "-", anchor)
    anchor = re.sub(r"-+", "-", anchor).strip("-")
    return anchor


def generate_toc(headers: list[tuple[int, str]]) -> str:
    """Generate markdown TOC content from parsed headers."""
    if not headers:
        return ""

    min_level = min(level for level, _ in headers)
    slug_counts: dict[str, int] = {}
    toc_lines: list[str] = []

    for level, text in headers:
        base_slug = _slugify(text)
        count = slug_counts.get(base_slug, 0)
        slug_counts[base_slug] = count + 1
        slug = base_slug if count == 0 else f"{base_slug}-{count}"

        indent = "  " * (level - min_level)
        toc_lines.append(f"{indent}* [{text}](#{slug})")

    return "\n".join(toc_lines)


def replace_toc_section(content: str, new_toc: str) -> str:
    """Replace TOC block content between TOC and END markers."""
    toc_start = content.find(TOC_MARKER)
    if toc_start == -1:
        raise ValueError("TOC marker not found")

    toc_end = content.find(END_MARKER, toc_start + len(TOC_MARKER))
    if toc_end == -1:
        raise ValueError("END marker not found after TOC marker")

    replacement = f"{TOC_MARKER}\n\n{new_toc}\n\n{END_MARKER}"
    return content[:toc_start] + replacement + content[toc_end + len(END_MARKER) :]


def build_expected_content(content: str) -> str:
    """Build the expected markdown content after TOC regeneration."""
    headers = parse_headers(content)
    toc = generate_toc(headers)
    return replace_toc_section(content, toc)


def resolve_markdown_files(inputs: list[str]) -> list[Path]:
    """Resolve CLI arguments to a de-duplicated ordered list of markdown files."""
    files: list[Path] = []
    seen: set[Path] = set()

    def add_path(candidate: Path) -> None:
        resolved = candidate.resolve()
        if resolved.suffix.lower() != ".md" or not resolved.is_file() or resolved in seen:
            return
        seen.add(resolved)
        files.append(resolved)

    for item in inputs:
        path = Path(item)
        if path.exists():
            if path.is_file():
                add_path(path)
            elif path.is_dir():
                for md_file in sorted(path.rglob("*.md")):
                    add_path(md_file)
            continue

        for matched in sorted(glob.glob(item, recursive=True)):
            add_path(Path(matched))

    return files


def verify_file(path: Path) -> bool:
    """Verify whether a file already contains the expected TOC."""
    content = path.read_text(encoding="utf-8")

    if TOC_MARKER not in content or END_MARKER not in content:
        print(f"SKIP    | {path} (missing TOC markers)")
        return True

    expected = build_expected_content(content)
    if expected == content:
        print(f"OK      | {path}")
        return True

    print(f"OUTDATED| {path}", file=sys.stderr)
    diff = difflib.unified_diff(
        content.splitlines(),
        expected.splitlines(),
        fromfile=f"{path} (current)",
        tofile=f"{path} (expected)",
        lineterm="",
    )
    for line in diff:
        print(line, file=sys.stderr)
    return False


def update_file(path: Path) -> bool:
    """Regenerate and write TOC in a markdown file."""
    content = path.read_text(encoding="utf-8")

    if TOC_MARKER not in content or END_MARKER not in content:
        print(f"SKIP    | {path} (missing TOC markers)")
        return True

    updated = build_expected_content(content)
    if updated == content:
        print(f"UNCHANGED| {path}")
        return True

    path.write_text(updated, encoding="utf-8")
    print(f"UPDATED | {path}")
    return True


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Generate markdown TOCs between <!--- TOC --> and <!--- END --> markers."
    )
    parser.add_argument("markdown_files", nargs="+", help="Markdown files, directories, or glob patterns")
    parser.add_argument(
        "--verify",
        action="store_true",
        help="Check files without modifying them; returns non-zero when TOC is outdated.",
    )
    args = parser.parse_args()

    files = resolve_markdown_files(args.markdown_files)
    if not files:
        print("No markdown files were resolved from input arguments.", file=sys.stderr)
        return 1

    results = [verify_file(path) if args.verify else update_file(path) for path in files]
    return 0 if all(results) else 1


if __name__ == "__main__":
    sys.exit(main())

