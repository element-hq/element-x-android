#!/usr/bin/python3
# encoding: utf-8
# SPDX-FileCopyrightText: 2024 FC (Fay) Stegerman <flx@obfusk.net>
# SPDX-License-Identifier: GPL-3.0-or-later.

import argparse
import os
import shutil
import subprocess
import sys
import tempfile

from typing import Optional, Tuple

COMMANDS = (
    "fix-compresslevel",
    "fix-files",
    "fix-newlines",
    "fix-pg-map-id",
    "rm-files",
    "sort-apk",
    "sort-baseline",
)

BUILD_TOOLS_WITH_BROKEN_ZIPALIGN = ("31.0.0", "32.0.0")
BUILD_TOOLS_WITH_PAGE_SIZE_FROM = "35.0.0-rc1"
SDK_ENV = ("ANDROID_HOME", "ANDROID_SDK", "ANDROID_SDK_ROOT")


def _zipalign_cmd(page_align: bool, page_size: Optional[int]) -> Tuple[str, ...]:
    if page_align:
        if page_size is not None:
            return ("zipalign", "-P", str(page_size), "4")
        return ("zipalign", "-p", "4")
    return ("zipalign", "4")


ZIPALIGN = _zipalign_cmd(page_align=False, page_size=None)
ZIPALIGN_P = _zipalign_cmd(page_align=True, page_size=None)


class Error(RuntimeError):
    pass


def inplace_fix(command: str, input_file: str, *args: str,
                zipalign: bool = False, page_align: bool = False,
                page_size: Optional[int] = None, internal: bool = False) -> None:
    if command not in COMMANDS:
        raise Error(f"Unknown command {command}")
    exe, script = _script_cmd(command)
    ext = os.path.splitext(input_file)[1]
    with tempfile.TemporaryDirectory() as tdir:
        fixed = os.path.join(tdir, "fixed" + ext)
        run_command(exe, script, input_file, fixed, *args, trim=2)
        if zipalign:
            aligned = os.path.join(tdir, "aligned" + ext)
            zac = zipalign_cmd(page_align=page_align, page_size=page_size, internal=internal)
            run_command(*zac, fixed, aligned, trim=2)
            print(f"[MOVE] {aligned} to {input_file}")
            shutil.move(aligned, input_file)
        else:
            print(f"[MOVE] {fixed} to {input_file}")
            shutil.move(fixed, input_file)


def zipalign_cmd(page_align: bool = False, page_size: Optional[int] = None,
                 internal: bool = False) -> Tuple[str, ...]:
    """
    Find zipalign command using $PATH or $ANDROID_HOME etc.

    >>> zipalign_cmd()
    ('zipalign', '4')
    >>> zipalign_cmd(page_align=True)
    ('zipalign', '-p', '4')
    >>> zipalign_cmd(page_align=True, page_size=16)
    ('zipalign', '-P', '16', '4')
    >>> cmd = zipalign_cmd(page_align=True, page_size=16, internal=True)
    >>> [x.split("/")[-1] for x in cmd]
    ['python3', 'zipalign.py', '-P', '16', '4']
    >>> os.environ["PATH"] = ""
    >>> for k in SDK_ENV:
    ...     os.environ[k] = ""
    >>> cmd = zipalign_cmd()
    >>> [x.split("/")[-1] for x in cmd]
    ['python3', 'zipalign.py', '4']
    >>> os.environ["ANDROID_HOME"] = "test/fake-sdk"
    >>> zipalign_cmd()
    [SKIP BROKEN] 31.0.0
    [FOUND] test/fake-sdk/build-tools/30.0.3/zipalign
    ('test/fake-sdk/build-tools/30.0.3/zipalign', '4')
    >>> cmd = zipalign_cmd(page_align=True, page_size=16)
    [SKIP TOO OLD] 31.0.0
    [SKIP TOO OLD] 30.0.3
    [SKIP TOO OLD] 26.0.2
    >>> [x.split("/")[-1] for x in cmd]
    ['python3', 'zipalign.py', '-P', '16', '4']
    >>> os.environ["ANDROID_HOME"] = "test/fake-sdk-2"
    >>> zipalign_cmd(page_align=True, page_size=16)
    [FOUND] test/fake-sdk-2/build-tools/35.0.0-rc1/zipalign
    ('test/fake-sdk-2/build-tools/35.0.0-rc1/zipalign', '-P', '16', '4')

    """
    cmd, *args = _zipalign_cmd(page_align, page_size)
    if not internal:
        if shutil.which(cmd):
            return (cmd, *args)
        for k in SDK_ENV:
            if home := os.environ.get(k):
                tools = os.path.join(home, "build-tools")
                if os.path.exists(tools):
                    for vsn in sorted(os.listdir(tools), key=_vsn, reverse=True):
                        if page_size and _vsn(vsn) < _vsn(BUILD_TOOLS_WITH_PAGE_SIZE_FROM):
                            print(f"[SKIP TOO OLD] {vsn}")
                            continue
                        for s in BUILD_TOOLS_WITH_BROKEN_ZIPALIGN:
                            if vsn.startswith(s):
                                print(f"[SKIP BROKEN] {vsn}")
                                break
                        else:
                            c = os.path.join(tools, vsn, cmd)
                            if shutil.which(c):
                                print(f"[FOUND] {c}")
                                return (c, *args)
    return (*_script_cmd(cmd), *args)


def _vsn(v: str) -> Tuple[int, ...]:
    """
    >>> vs = "31.0.0 32.1.0-rc1 34.0.0-rc3 34.0.0 35.0.0-rc1".split()
    >>> for v in sorted(vs, key=_vsn, reverse=True):
    ...     (_vsn(v), v)
    ((35, 0, 0, 0, 1), '35.0.0-rc1')
    ((34, 0, 0, 1, 0), '34.0.0')
    ((34, 0, 0, 0, 3), '34.0.0-rc3')
    ((32, 1, 0, 0, 1), '32.1.0-rc1')
    ((31, 0, 0, 1, 0), '31.0.0')
    """
    if "-rc" in v:
        v = v.replace("-rc", ".0.", 1)
    else:
        v = v + ".1.0"
    return tuple(int(x) if x.isdigit() else -1 for x in v.split("."))


def _script_cmd(command: str) -> Tuple[str, str]:
    script_dir = os.path.dirname(__file__)
    for cmd in (command, command.replace("-", "_")):
        script = os.path.join(script_dir, cmd + ".py")
        if os.path.exists(script):
            break
    else:
        raise Error(f"Script for {command} not found")
    exe = sys.executable or "python3"
    return exe, script


def run_command(*args: str, trim: int = 1) -> None:
    targs = tuple(os.path.basename(a) for a in args[:trim]) + args[trim:]
    print(f"[RUN] {' '.join(targs)}")
    try:
        subprocess.run(args, check=True)
    except subprocess.CalledProcessError as e:
        raise Error(f"{args[0]} command failed") from e
    except FileNotFoundError as e:
        raise Error(f"{args[0]} command not found") from e


def main() -> None:
    prog = os.path.basename(sys.argv[0])
    usage = (f"{prog} [-h] [--zipalign] [--page-align] [--page-size N] [--internal]\n"
             f"{len('usage: ' + prog) * ' '} COMMAND INPUT_FILE [...]")
    epilog = f"Commands: {', '.join(COMMANDS)}."
    parser = argparse.ArgumentParser(usage=usage, epilog=epilog)
    parser.add_argument("--zipalign", action="store_true",
                        help="run zipalign after COMMAND")
    parser.add_argument("--page-align", action="store_true",
                        help="run zipalign w/ -p option (implies --zipalign)")
    parser.add_argument("--page-size", metavar="N", type=int,
                        help="run zipalign w/ -P N option (implies --page-align)")
    parser.add_argument("--internal", action="store_true",
                        help="use zipalign.py instead of searching $PATH/$ANDROID_HOME/etc.")
    parser.add_argument("command", metavar="COMMAND")
    parser.add_argument("input_file", metavar="INPUT_FILE")
    args, rest = parser.parse_known_args()
    try:
        inplace_fix(args.command, args.input_file, *rest,
                    zipalign=bool(args.zipalign or args.page_align or args.page_size),
                    page_align=bool(args.page_align or args.page_size),
                    page_size=args.page_size, internal=args.internal)
    except Error as e:
        print(f"Error: {e}.", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()

# vim: set tw=80 sw=4 sts=4 et fdm=marker :
