#!/usr/bin/python3
# encoding: utf-8
# SPDX-FileCopyrightText: 2024 FC (Fay) Stegerman <flx@obfusk.net>
# SPDX-License-Identifier: GPL-3.0-or-later.

import hashlib
import os
import re
import struct
import zipfile
import zlib

from binascii import hexlify
from typing import Any, Dict, Match, Tuple

DEX_MAGIC = b"dex\n"
DEX_MAGIC_RE = re.compile(rb"dex\n(\d{3})\x00")

PROF_MAGIC = b"pro\x00"
PROF_010_P = b"010\x00"

CLASSES_DEX_RE = re.compile(r"classes\d*\.dex")
ASSET_PROF = "assets/dexopt/baseline.prof"

PG_MAP_ID_RE = re.compile(rb'(~~R8{"backend":"dex".*?"pg-map-id":")([0-9a-f]{7})(")')

ATTRS = ("compress_type", "create_system", "create_version", "date_time",
         "external_attr", "extract_version", "flag_bits")
LEVELS = (9, 6, 4, 1)


class Error(RuntimeError):
    pass


# FIXME: is there a better alternative?
class ReproducibleZipInfo(zipfile.ZipInfo):
    """Reproducible ZipInfo hack."""

    if "_compresslevel" not in zipfile.ZipInfo.__slots__:       # type: ignore[attr-defined]
        if "compress_level" not in zipfile.ZipInfo.__slots__:   # type: ignore[attr-defined]
            raise Error("zipfile.ZipInfo has no ._compresslevel")

    _compresslevel: int
    _override: Dict[str, Any] = {}

    def __init__(self, zinfo: zipfile.ZipInfo, **override: Any) -> None:
        # pylint: disable=W0231
        if override:
            self._override = {**self._override, **override}
        for k in self.__slots__:
            if hasattr(zinfo, k):
                setattr(self, k, getattr(zinfo, k))

    def __getattribute__(self, name: str) -> Any:
        if name != "_override":
            try:
                return self._override[name]
            except KeyError:
                pass
        return object.__getattribute__(self, name)


def fix_pg_map_id(input_dir: str, output_dir: str, map_id: str) -> None:
    file_data = {}
    for filename in [ASSET_PROF] + sorted(os.listdir(input_dir)):
        if re.fullmatch(CLASSES_DEX_RE, filename) or filename == ASSET_PROF:
            print(f"reading {filename!r}...")
            with open(os.path.join(input_dir, *filename.split("/")), "rb") as fh:
                file_data[filename] = fh.read()
    _fix_pg_map_id(file_data, map_id)
    for filename, data in file_data.items():
        print(f"writing {filename!r}...")
        if "/" in filename:
            os.makedirs(os.path.join(output_dir, *filename.split("/")[:-1]), exist_ok=True)
        with open(os.path.join(output_dir, *filename.split("/")), "wb") as fh:
            fh.write(data)


def fix_pg_map_id_apk(input_apk: str, output_apk: str, map_id: str) -> None:
    with open(input_apk, "rb") as fh_raw:
        with zipfile.ZipFile(input_apk) as zf_in:
            with zipfile.ZipFile(output_apk, "w") as zf_out:
                file_data = {}
                for info in zf_in.infolist():
                    if re.fullmatch(CLASSES_DEX_RE, info.filename) or info.filename == ASSET_PROF:
                        print(f"reading {info.filename!r}...")
                        file_data[info.filename] = zf_in.read(info)
                _fix_pg_map_id(file_data, map_id)
                for info in zf_in.infolist():
                    attrs = {attr: getattr(info, attr) for attr in ATTRS}
                    zinfo = ReproducibleZipInfo(info, **attrs)
                    if info.compress_type == 8:
                        fh_raw.seek(info.header_offset)
                        n, m = struct.unpack("<HH", fh_raw.read(30)[26:30])
                        fh_raw.seek(info.header_offset + 30 + m + n)
                        ccrc = 0
                        size = info.compress_size
                        while size > 0:
                            ccrc = zlib.crc32(fh_raw.read(min(size, 4096)), ccrc)
                            size -= 4096
                        with zf_in.open(info) as fh_in:
                            comps = {lvl: zlib.compressobj(lvl, 8, -15) for lvl in LEVELS}
                            ccrcs = {lvl: 0 for lvl in LEVELS}
                            while True:
                                data = fh_in.read(4096)
                                if not data:
                                    break
                                for lvl in LEVELS:
                                    ccrcs[lvl] = zlib.crc32(comps[lvl].compress(data), ccrcs[lvl])
                            for lvl in LEVELS:
                                if ccrc == zlib.crc32(comps[lvl].flush(), ccrcs[lvl]):
                                    zinfo._compresslevel = lvl
                                    break
                            else:
                                raise Error(f"Unable to determine compresslevel for {info.filename!r}")
                    elif info.compress_type != 0:
                        raise Error(f"Unsupported compress_type {info.compress_type}")
                    if re.fullmatch(CLASSES_DEX_RE, info.filename) or info.filename == ASSET_PROF:
                        print(f"writing {info.filename!r}...")
                        zf_out.writestr(zinfo, file_data[info.filename])
                    else:
                        with zf_in.open(info) as fh_in:
                            with zf_out.open(zinfo, "w") as fh_out:
                                while True:
                                    data = fh_in.read(4096)
                                    if not data:
                                        break
                                    fh_out.write(data)


def _fix_pg_map_id(file_data: Dict[str, bytes], map_id: str) -> None:
    crcs = {}
    for filename in file_data:
        if re.fullmatch(CLASSES_DEX_RE, filename):
            print(f"fixing {filename!r}...")
            data = _fix_dex_id_checksum(file_data[filename], map_id.encode())
            file_data[filename] = data
            crcs[filename] = zlib.crc32(data)
    if ASSET_PROF in file_data:
        print(f"fixing {ASSET_PROF!r}...")
        file_data[ASSET_PROF] = _fix_prof_checksum(file_data[ASSET_PROF], crcs)


def _fix_dex_id_checksum(data: bytes, map_id: bytes) -> bytes:
    def repl(m: Match[bytes]) -> bytes:
        print(f"fixing pg-map-id: {m.group(2)!r} -> {map_id!r}")
        return m.group(1) + map_id + m.group(3)

    magic = data[:8]
    if magic[:4] != DEX_MAGIC or not DEX_MAGIC_RE.fullmatch(magic):
        raise Error(f"Unsupported magic {magic!r}")
    print(f"dex version={int(magic[4:7]):03d}")
    checksum, signature = struct.unpack("<I20s", data[8:32])
    fixed_data = re.sub(PG_MAP_ID_RE, repl, data[32:])
    if fixed_data == data[32:]:
        print("(not modified)")
        return data
    fixed_sig = hashlib.sha1(fixed_data).digest()
    print(f"fixing signature: {hexlify(signature).decode()} -> {hexlify(fixed_sig).decode()}")
    fixed_data = fixed_sig + fixed_data
    fixed_checksum = zlib.adler32(fixed_data)
    print(f"fixing checksum: 0x{checksum:x} -> 0x{fixed_checksum:x}")
    return magic + int.to_bytes(fixed_checksum, 4, "little") + fixed_data


def _fix_prof_checksum(data: bytes, crcs: Dict[str, int]) -> bytes:
    magic, data = _split(data, 4)
    version, data = _split(data, 4)
    if magic == PROF_MAGIC:
        if version == PROF_010_P:
            print("prof version=010 P")
            return PROF_MAGIC + PROF_010_P + _fix_prof_010_p_checksum(data, crcs)
        else:
            raise Error(f"Unsupported prof version {version!r}")
    else:
        raise Error(f"Unsupported magic {magic!r}")


def _fix_prof_010_p_checksum(data: bytes, crcs: Dict[str, int]) -> bytes:
    num_dex_files, uncompressed_data_size, compressed_data_size, data = _unpack("<BII", data)
    dex_data_headers = []
    if len(data) != compressed_data_size:
        raise Error("Compressed data size does not match")
    data = zlib.decompress(data)
    if len(data) != uncompressed_data_size:
        raise Error("Uncompressed data size does not match")
    for i in range(num_dex_files):
        profile_key_size, num_type_ids, hot_method_region_size, \
            dex_checksum, num_method_ids, data = _unpack("<HHIII", data)
        profile_key, data = _split(data, profile_key_size)
        filename = profile_key.decode()
        fixed_checksum = crcs[filename]
        if fixed_checksum != dex_checksum:
            print(f"fixing {filename!r} checksum: 0x{dex_checksum:x} -> 0x{fixed_checksum:x}")
        dex_data_headers.append(struct.pack(
            "<HHIII", profile_key_size, num_type_ids, hot_method_region_size,
            fixed_checksum, num_method_ids) + profile_key)
    fixed_data = b"".join(dex_data_headers) + data
    fixed_cdata = zlib.compress(fixed_data, 1)
    fixed_hdr = struct.pack("<BII", num_dex_files, uncompressed_data_size, len(fixed_cdata))
    return fixed_hdr + fixed_cdata


def _unpack(fmt: str, data: bytes) -> Any:
    assert all(c in "<BHI" for c in fmt)
    size = fmt.count("B") + 2 * fmt.count("H") + 4 * fmt.count("I")
    return struct.unpack(fmt, data[:size]) + (data[size:],)


def _split(data: bytes, size: int) -> Tuple[bytes, bytes]:
    return data[:size], data[size:]


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(prog="fix-pg-map-id.py")
    parser.add_argument("input_dir_or_apk", metavar="INPUT_DIR_OR_APK")
    parser.add_argument("output_dir_or_apk", metavar="OUTPUT_DIR_OR_APK")
    parser.add_argument("pg_map_id", metavar="PG_MAP_ID")
    args = parser.parse_args()
    if os.path.isdir(args.input_dir_or_apk):
        fix_pg_map_id(args.input_dir_or_apk, args.output_dir_or_apk, args.pg_map_id)
    else:
        fix_pg_map_id_apk(args.input_dir_or_apk, args.output_dir_or_apk, args.pg_map_id)

# vim: set tw=80 sw=4 sts=4 et fdm=marker :
