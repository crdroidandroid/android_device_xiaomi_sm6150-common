#define LOG_TAG "libqti-perfd-client"

#include <stdint.h>
#include <shared_mutex>
#include <string>

#include <android/binder_manager.h>
#include <android/log.h>
#include <binder/IServiceManager.h>

#include <aidl/android/hardware/power/IPower.h>

using aidl::android::hardware::power::Boost;
using aidl::android::hardware::power::IPower;
using aidl::android::hardware::power::Mode;
using IPowerAidl = aidl::android::hardware::power::IPower;

namespace android {
class PowerAidl {
    std::shared_ptr<IPowerAidl> power_hal_aidl_;

  public:
    PowerAidl() {
        const std::string kInstance = std::string(IPower::descriptor) + "/default";
        ndk::SpAIBinder power_binder =
                ndk::SpAIBinder(AServiceManager_getService(kInstance.c_str()));
        if (power_binder.get() == nullptr) {
            ALOGE("Cannot get Power Hal Binder");
            return;
        }

        power_hal_aidl_ = IPower::fromBinder(power_binder);
        if (power_hal_aidl_ == nullptr) {
            ALOGE("Cannot get Power Hal AIDL");
        }
    }

    void setMode(Mode hint, bool enabled) {
        if (power_hal_aidl_ == nullptr) {
            ALOGE("power_hal_aidl_ is null!");
            return;
        }

        auto ret = power_hal_aidl_->setMode(hint, enabled);
        if (!ret.isOk()) {
            ALOGE("Set mode %s to %d failed!", toString(hint).c_str(), enabled);
        }
    }

    void setBoost(Boost hint, int32_t duration) {
        if (power_hal_aidl_ == nullptr) {
            ALOGE("power_hal_aidl_ is null!");
            return;
        }

        auto ret = power_hal_aidl_->setBoost(hint, duration);
        if (!ret.isOk()) {
            ALOGE("Set boost %s for %dms failed!", toString(hint).c_str(), duration);
        }
    }
};

static PowerAidl poweraidl = PowerAidl();
static int handleNum = 0;

extern "C" void perf_get_feedback() {}
extern "C" void perf_hint() {}
extern "C" void perf_lock_cmd() {}
extern "C" void perf_lock_use_profile() {}

extern "C" int perf_lock_acq(int handle, int duration_ms, int params[], int size) {
    int ret = 0; /* Default to error - not acquired */

    switch (size) {
        case 14: /* CAMERA_LAUNCH */
            poweraidl.setBoost(Boost::CAMERA_LAUNCH, 0);
            ALOGI("perf_lock_acq: CAMERA_LAUNCH");
            break;

        case 20: /* CAMERA_SHOT */
            poweraidl.setBoost(Boost::CAMERA_SHOT, 0);
            ALOGI("perf_lock_acq: CAMERA_SHOT");
            break;

        case 22: /* Streaming 4k */
            poweraidl.setMode(Mode::CAMERA_STREAMING_HIGH, true);
            ALOGI("perf_lock_acq: CAMERA_STREAMING_HIGH");
            break;

        case 30: /* Streaming 1080p60fps */
            poweraidl.setMode(Mode::CAMERA_STREAMING_MID, true);
            ALOGI("perf_lock_acq: CAMERA_STREAMING_MID");
            break;

        case 26: /* Streaming 720p/1080p30fps */
            poweraidl.setMode(Mode::CAMERA_STREAMING_LOW, true);
            ALOGI("perf_lock_acq: CAMERA_STREAMING_LOW");
            break;

        case 10: /* CAMERA_CLOSE - release all handles */
            if (handleNum != 0) {
                handleNum = 0;

                poweraidl.setMode(Mode::CAMERA_STREAMING_LOW, false);
                poweraidl.setMode(Mode::CAMERA_STREAMING_MID, false);
                poweraidl.setMode(Mode::CAMERA_STREAMING_HIGH, false);
            }

            ALOGI("perf_lock_acq: CAMERA_CLOSE");
            return ret;
            break;

        default: /* Not implemented */
            ALOGI("perf_lock_acq: handle: %d, duration_ms: %d, params[0]: %d, size: %d", handle,
                  duration_ms, params[0], size);
            return ret;
            break;
    }

    if (handle == 0) {
        ret = ++handleNum;
    } else {
        ret = handle;
    }

    ALOGI("perf_lock_acq: returning handle val: %d", ret);
    return ret;
}

extern "C" int perf_lock_rel(int handle) {
    ALOGI("perf_lock_rel: handle: %d", handle);
    return 0;  // success
}
}  // namespace android
