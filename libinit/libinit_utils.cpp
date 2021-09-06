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
    nullptr,   "product.", "product_services.", "odm.",
    "vendor.", "system.", "system_ext.", "bootimage.",
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
