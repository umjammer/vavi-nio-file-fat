[![](https://jitpack.io/v/umjammer/vavi-nio-file-fat.svg)](https://jitpack.io/#umjammer/vavi-nio-file-fat)
[![Java CI](https://github.com/umjammer/vavi-nio-file-fat/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-nio-file-fat/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-nio-file-fat/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/vavi-nio-file-fat/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--apps--fuse-pink)](https://github.com/umjammer/vavi-apps-fuse)

# vavi-nio-file-fat

<img alt="logo" src="src/test/resources/duke_fat.png" width="200"/>

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

 * [maven](https://jitpack.io/#umjammer/vavi-nio-file-fat)

## Usage


### bpb validation spi

`src/main/resources/META-INF/services/vavix.io.partition.Validator`

### system properties

<!-- * `discUtils.core.file.encoding` ... filename encoding for `Charset#forName(String)`, default is `MS932` -->
* `vavix.io.partition.validator.fat` ... validator for finding fat literal default is `false`
* `vavix.io.partition.validator.ipl` ... validator for finding ipl literal default is `true`
* `vavix.io.partition.validator.nec` ... validator for finding nec literal, default is `true`

### sample

 * [fat32 formated iPod salvage](src/test/java/ipod)

## References

 * https://github.com/barbeque/pc98-disk-tools
 * https://www.pc98.org/main.html
 * http://elm-chan.org/docs/fat.html

## TODO

 * file entry related class tree is not good
 * ~~import bpb validation system from jnode and discutils~~

---

<sub>image designed by @umjammer, drawn by nano banana</sub>
