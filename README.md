[![](https://jitpack.io/v/umjammer/vavi-nio-file-fat.svg)](https://jitpack.io/#umjammer/vavi-nio-file-fat)
[![Java CI](https://github.com/umjammer/vavi-nio-file-fat/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-nio-file-fat/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-nio-file-fat/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/vavi-nio-file-fat/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--apps--fuse-pink)](https://github.com/umjammer/vavi-apps-fuse)

# vavi-nio-file-fat

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/6/60/Fat32_structure.svg/837px-Fat32_structure.svg.png" width="220" alt="fat icon"/><sub><a href="https://creativecommons.org/licenses/by-sa/3.0/">CC BY SA 3.0</a></sub>

🌏 mount the old school world!

### Status

| fs         | list | upload | download | copy | move | rm | mkdir | cache |
|------------|------|--------|----------|------|------|----|-------|-------|
| FAT12 (98) | ✅   |        | ✅        |      |   |  |    |    |
| FAT12 (AT) | ✅   |        | ?        |      |   |  |    |    |
| FAT16 (98) | ✅   |        | ✅        |      |   |  |    |    |
| FAT16 (AT) | ✅   |        | ✅        |      |   |  |    |    |
| FAT32 (AT) | ✅   |        | ✅        |      |   |  |    |    |

## Install

 * [jitpack](https://jitpack.io/#umjammer/vavi-nio-file-fat)

## Usage

### sample

 * [fat32 formated iPod salvage](src/test/java/ipod)

## References

 * https://github.com/barbeque/pc98-disk-tools
 * https://www.pc98.org/main.html
 * http://elm-chan.org/docs/fat.html

## TODO

 * file entry related class tree is not good