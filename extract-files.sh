#!/bin/bash
#
# Copyright (C) 2018-2020 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

set -e

# Load extract_utils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "${MY_DIR}" ]]; then MY_DIR="${PWD}"; fi

LINEAGE_ROOT="${MY_DIR}"/../../..

HELPER="${LINEAGE_ROOT}/vendor/lineage/build/tools/extract_utils.sh"
if [ ! -f "${HELPER}" ]; then
    echo "Unable to find helper script at ${HELPER}"
    exit 1
fi
source "${HELPER}"

# Default to sanitizing the vendor folder before extraction
CLEAN_VENDOR=true

ONLY_COMMON=
SECTION=
KANG=

while [ "${#}" -gt 0 ]; do
    case "${1}" in
        -o | --only-common )
                ONLY_COMMON=false
                ;;
        -n | --no-cleanup )
                CLEAN_VENDOR=false
                ;;
        -k | --kang )
                KANG="--kang"
                ;;
        -s | --section )
                SECTION="${2}"; shift
                CLEAN_VENDOR=false
                ;;
        * )
                SRC="${1}"
                ;;
    esac
    shift
done

if [ -z "${SRC}" ]; then
    SRC="adb"
fi

function blob_fixup() {
    case "${1}" in
    vendor/bin/mlipayd@1.1 | vendor/lib64/libmlipay.so | vendor/lib64/libmlipay@1.1.so )
        patchelf --remove-needed vendor.xiaomi.hardware.mtdservice@1.0.so "${2}"
    ;;
    lib64/libwfdnative.so | lib64/libfm-hci.so | lib/libfm-hci.so |  vendor/lib64/vendor.xiaomi.hardware.citsensorservice@1.0.so | \
            vendor/lib64/vendor.xiaomi.hardware.citsensorservice@1.1.so | vendor/lib64/libgoodixhwfingerprint.so | \
            vendor/lib/vendor.qti.hardware.scve.panorama@1.0-halimpl.so | vendor/lib/vendor.qti.hardware.scve.objecttracker@1.0-halimpl.so | \
            vendor/bin/hw/vendor.qti.hardware.scve.panorama@1.0-service | vendor/bin/hw/vendor.qti.hardware.scve.objecttracker@1.0-service )
        patchelf --remove-needed "android.hidl.base@1.0.so" "${2}"
    ;;
    vendor/etc/camera/camxoverridesettings.txt )
        sed -i "s|0x10080|0|g" "${2}"
        sed -i "s|0x1F|0x0|g" "${2}"
    ;;
    esac
}

# Initialize the helper for common device
setup_vendor "${DEVICE_COMMON}" "${VENDOR}" "${LINEAGE_ROOT}" true "${CLEAN_VENDOR}"

extract "${MY_DIR}/proprietary-files.txt" "${SRC}" \
        "${KANG}" --section "${SECTION}"

if [ -z "${ONLY_COMMON}" ] && [ -s "${MY_DIR}/../${DEVICE}/proprietary-files.txt" ]; then
    # Reinitialize the helper for device
    source "${MY_DIR}/../${DEVICE}/extract-files.sh"
    setup_vendor "${DEVICE}" "${VENDOR}" "${LINEAGE_ROOT}" false "${CLEAN_VENDOR}"

    extract "${MY_DIR}/../${DEVICE}/proprietary-files.txt" "${SRC}" \
            "${KANG}" --section "${SECTION}"
fi

COMMON_BLOB_ROOT="${LINEAGE_ROOT}/vendor/${VENDOR}/${DEVICE_COMMON}/proprietary"

#
# Fix product path
#
function fix_product_path () {
    sed -i \
        's/\/system\/framework\//\/system\/product\/framework\//g' \
        "$COMMON_BLOB_ROOT"/"$1"
}

fix_product_path product/etc/permissions/vendor.qti.hardware.factory.xml
fix_product_path product/etc/permissions/vendor-qti-hardware-sensorscalibrate.xml

#
# Fix xml version
#
function fix_xml_version () {
    sed -i \
        's/xml version="2.0"/xml version="1.0"/' \
        "$COMMON_BLOB_ROOT"/"$1"
}

fix_xml_version product/etc/permissions/vendor.qti.hardware.data.connection-V1.0-java.xml
fix_xml_version product/etc/permissions/vendor.qti.hardware.data.connection-V1.1-java.xml

"${MY_DIR}/setup-makefiles.sh"
