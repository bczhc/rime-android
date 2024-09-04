FROM ubuntu

COPY / /app/

ARG ndk_version=25.1.8937393
ARG cmake_version=3.18.1
ARG full_targets='armeabi-v7a-21,arm64-v8a-29,x86-29,x86_64-29'

WORKDIR /

RUN apt update && \
    export DEBIAN_FRONTEND=noninteractive && \
    apt install openjdk-17-jdk unzip curl gcc git cmake make ninja-build wget ruby opencc -y && \
    # See https://github.com/bczhc/some-tools/blob/aa86a52331415291cb1bea940eceba7d34d7ece4/Dockerfile#L14-L26
    apt install clang gcc-multilib -y

# Set up SDK
RUN mkdir sdk && \
    wget 'https://dl.google.com/android/repository/commandlinetools-linux-8092744_latest.zip' -O tools.zip && \
    unzip tools.zip && \
    yes | ./cmdline-tools/bin/sdkmanager --licenses --sdk_root=./sdk && \
    ./cmdline-tools/bin/sdkmanager --sdk_root=./sdk --install "ndk;$ndk_version" && \
    ./cmdline-tools/bin/sdkmanager --sdk_root=./sdk --install "cmake;$cmake_version"

# Fetch Trime submodule and build librime from it
RUN cd /app && \
    git submodule update --init --recursive && \
    cd trime && \
    git apply /app/trime.patch && \
    cd app/src/main/jni && \
    /app/tools/trime-build-librime "/sdk/ndk/$ndk_version" && \
    [ -f ./librime/src/rime_api.h ]


WORKDIR /app

# Set up configurations
RUN echo 'sdk.dir=/sdk' > local.properties && \
    echo "ndk.dir=/sdk/ndk/$ndk_version" >> local.properties && \
    echo "ndk.target=$full_targets" >> config.properties && \
    echo 'ndk.buildType=release' >> config.properties && \
    echo 'librime-lib-dir=/app/trime/app/src/main/jni/libs' >> config.properties && \
    echo 'librime-include-dir=/app/trime/app/src/main/jni/librime/src' >> config.properties


# Install Rust
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs > install && \
    chmod +x install && \
    ./install -y && \
    . ~/.cargo/env && \
    rustup default nightly-2024-09-01 && \
    rustc --version && \
    ./tools/configure-rust

# Build
RUN mkdir /apks && \
    . ~/.cargo/env && \
    for target in $(echo $full_targets | sed "s/,/ /g"); do \
      echo "Build variant: $target" && \
      sed -ri "s/^(ndk\.target)=.*/\1=$target/" config.properties && \
      ./gradlew asR && \
      cp -v app/build/outputs/apk/release/app-release.apk /apks/$target.apk; \
    done && \
    echo "Build variant: universal" && \
    sed -ri "s/^(ndk\.target)=.*/\1=$full_targets/" config.properties && \
    ./gradlew asR && \
    cp -v app/build/outputs/apk/release/app-release.apk /apks/universal.apk

