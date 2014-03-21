#!/bin/sh

if [ x"$ANDROID_SOURCE" = x"" ]; then
	ANDROID_SOURCE=$PWD
fi

# host
HOST=`uname -s | tr 'A-Z' 'a-z'`
# target
TARGET=arm-eabi
# toolchain version
TOOLCHAIN_VERSION="4.4.3"
# path
PATH=$ANDROID_SOURCE/prebuilt/$HOST-x86/toolchain/$TARGET-$TOOLCHAIN_VERSION/bin:$PATH
# the fullpath of libgcc.a
LIBGCCA=`ls $ANDROID_SOURCE/prebuilt/$HOST-x86/toolchain/$TARGET-$TOOLCHAIN_VERSION/lib/gcc/$TARGET/*/thumb/libgcc.a`

# the path of openssl
OPENSSL_PREFIX=$ANDROID_SOURCE/external/openssl
# the path of libcrypto.so libssl.so, can get it from /system/lib
OUT_LIBDIR=$ANDROID_SOURCE/out/target/product/generic/system/lib

CURL_VERSION=7.33.0
C_ARES_VERSION=1.10.0
CURL_EXTRA="--disable-file --disable-ldap --disable-ldaps --disable-rtsp --disable-proxy --disable-dict --disable-telnet --disable-tftp --disable-pop3 --disable-imap --disable-smtp --disable-gopher --disable-sspi"

pushd `dirname $0`

rm -rf curl curl-$CURL_VERSION
tar xf curl-$CURL_VERSION.tar.*
mv curl-$CURL_VERSION curl
mkdir -p curl/ares

rm -rf ares c-ares-$C_ARES_VERSION
tar xf c-ares-$C_ARES_VERSION.tar.*
mv c-ares-$C_ARES_VERSION ares

pushd curl
./configure CC=$TARGET-gcc --host=arm-linux \
	CPPFLAGS="-DANDROID -I$ANDROID_SOURCE/bionic/libc/arch-arm/include -I$ANDROID_SOURCE/bionic/libc/include -I$ANDROID_SOURCE/bionic/libc/kernel/common -I$ANDROID_SOURCE/bionic/libc/kernel/arch-arm -I$ANDROID_SOURCE/external/zlib" \
	CFLAGS="-fno-exceptions -Wno-multichar -mthumb-interwork -mthumb -nostdlib " \
	LIBS="-lc -ldl -lz $LIBGCCA " \
	LDFLAGS="-L$OUT_LIBDIR " \
	--enable-ipv6 --disable-manual --with-random=/dev/urandom \
	--with-ssl=$OPENSSL_PREFIX --without-ca-bundle --without-ca-path \
	--with-zlib --enable-ares $CURL_EXTRA || exit 1
popd

pushd ares
./configure CC=$TARGET-gcc --host=arm-linux \
	CPPFLAGS="-DANDROID -I$ANDROID_SOURCE/bionic/libc/arch-arm/include -I$ANDROID_SOURCE/bionic/libc/include -I$ANDROID_SOURCE/bionic/libc/kernel/common -I$ANDROID_SOURCE/bionic/libc/kernel/arch-arm" \
	CFLAGS="-fno-exceptions -Wno-multichar -mthumb-interwork -mthumb -nostdlib " \
	LIBS="-lc -ldl " \
	LDFLAGS="-L$OUT_LIBDIR "\
	--with-random=/dev/urandom || exit 1
popd

popd
