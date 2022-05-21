/*
 * Copyright (C) 2021 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>
#include <vector>

#include <libinit_utils.h>

constexpr const char *RO_PROP_SOURCES[] = {
    nullptr,   "product.", "product_services.", "odm.", "odm_dlkm.",
    "vendor.", "vendor_dlkm.", "system.", "system_ext.", "bootimage.",
};

void property_override(std::string prop, std::string value, bool add) {
    auto pi = (prop_info *) __system_property_find(prop.c_str());
    if (pi != nullptr) {
        __system_property_update(pi, value.c_str(), value.length());
    } else if (add) {
        __system_property_add(prop.c_str(), prop.length(), value.c_str(), value.length());
    }
}

void set_ro_build_prop(const std::string &prop, const std::string &value, bool product) {
    for (const auto &source : RO_PROP_SOURCES) {
        std::string prop_name = "ro.";

        if (product)
            prop_name += "product.";
        if (source != nullptr)
            prop_name += source;
        if (!product)
            prop_name += "build.";
        prop_name += prop;

        property_override(prop_name.c_str(), value);
    }
}

#define FIND_AND_REMOVE(s, delimiter, variable_name) \
    std::string variable_name = s.substr(0, s.find(delimiter)); \
    s.erase(0, s.find(delimiter) + delimiter.length());

#define APPEND_STRING(s, to_append) \
    s.append(" "); \
    s.append(to_append);

std::string fingerprint_to_description(std::string fingerprint) {
    std::string delimiter = "/";
    std::string delimiter2 = ":";
    std::string build_fingerprint_copy = fingerprint;

    FIND_AND_REMOVE(build_fingerprint_copy, delimiter, brand)
    FIND_AND_REMOVE(build_fingerprint_copy, delimiter, product)
    FIND_AND_REMOVE(build_fingerprint_copy, delimiter2, device)
    FIND_AND_REMOVE(build_fingerprint_copy, delimiter, platform_version)
    FIND_AND_REMOVE(build_fingerprint_copy, delimiter, build_id)
    FIND_AND_REMOVE(build_fingerprint_copy, delimiter2, build_number)
    FIND_AND_REMOVE(build_fingerprint_copy, delimiter, build_variant)
    std::string build_version_tags = build_fingerprint_copy;

    std::string description = product + "-" + build_variant;
    APPEND_STRING(description, platform_version)
    APPEND_STRING(description, build_id)
    APPEND_STRING(description, build_number)
    APPEND_STRING(description, build_version_tags)

    return description;
}
