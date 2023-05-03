RimeAndroid
---

## Build
Create `config.properties` in the root project, with its
content like this (just an example on my machine):
```properties
ndk.targets = arm64-v8a-29,x86-29,armeabi-v7a-21,x86_64-29
librime-lib-dir = /home/bczhc/bin/librime-libs
librime-include-dir = /home/bczhc/open-source/trime/app/src/main/jni/librime/src
```

Directory structure of `librime-lib-dir`:
```console
librime-libs/
├── arm64-v8a
│   └── librime.so
├── armeabi-v7a
│   └── librime.so
├── x86
│   └── librime.so
└── x86_64
    └── librime.so
```
You can follow this [gist](https://gist.github.com/bczhc/cd271734f812f32b4c9c70c766f1c95f) for
the build of these libraries.

For `librime-include-dir`, ensure `rime_api.h` exists under the location:
```console
~ ❯ ls /home/bczhc/open-source/trime/app/src/main/jni/librime/src/                                                           11:34:18
CMakeLists.txt  rime  rime_api.cc  rime_api.h  rime_levers_api.h
```

Then do `./gradlew asD` for debug build and `./gradlew asR` for release build.

