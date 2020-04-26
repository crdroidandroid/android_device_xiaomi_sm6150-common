#!/vendor/bin/sh

if [ $(getprop ro.boot.hwc) == "INDIA" ] && [ -d "/system/app/NQNfcNci" ]; then
    # POCO X2 doesn't have NFC chip
    rm -f /vendor/etc/permissions/android.hardware.nfc.xml
    rm -f /vendor/etc/permissions/android.hardware.nfc.ese.xml
    rm -f /vendor/etc/permissions/android.hardware.nfc.hce.xml
    rm -f /vendor/etc/permissions/android.hardware.nfc.hcef.xml
    rm -f /vendor/etc/permissions/android.hardware.nfc.uicc.xml
    rm -f /vendor/etc/permissions/com.android.nfc_extras.xml
    rm -f /vendor/etc/permissions/com.nxp.mifare.xml
    rm -f /vendor/lib/nfc_nci.nqx.default.hw.so
    rm -f /vendor/lib64/nfc_nci.nqx.default.hw.so
    rm -f /vendor/lib/libsn100u_fw.so
    rm -f /system/etc/nqnfcee_access.xml
    rm -f /system/etc/nqnfcse_access.xml
    rm -f /system/lib/libnqnfc-nci.so
    rm -f /system/lib64/libnqnfc-nci.so
    rm -f /system/lib64/libnqnfc_nci_jni.so
    rm -f /system/lib64/libsn100nfc-nci.so
    rm -f /system/lib64/libsn100nfc_nci_jni.so
    rm -f /system/etc/permissions/com.nxp.nfc.nq.xml
    rm -f /system/etc/permissions/com.android.nfc_extras.xml
    rm -f /system/etc/permissions/android.software.nfc.beam.xml
    rm -f /system/framework/com.nxp.nfc.nq.jar
    rm -f /system/framework/com.android.nfc_extras.jar
    rm -rf /system/app/NQNfcNci
fi
