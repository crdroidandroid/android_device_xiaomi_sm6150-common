#! /vendor/bin/sh

# Copyright (c) 2012-2013, 2016-2020, The Linux Foundation. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of The Linux Foundation nor
#       the names of its contributors may be used to endorse or promote
#       products derived from this software without specific prior written
#       permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NON-INFRINGEMENT ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
# OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
# ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

function configure_memory_parameters() {
    # Set Memory parameters.
    #
    # Set per_process_reclaim tuning parameters
    # All targets will use vmpressure range 50-70,
    # All targets will use 512 pages swap size.
    #
    # Set Low memory killer minfree parameters
    # 32 bit Non-Go, all memory configurations will use 15K series
    # 32 bit Go, all memory configurations will use uLMK + Memcg
    # 64 bit will use Google default LMK series.
    #
    # Set ALMK parameters (usually above the highest minfree values)
    # vmpressure_file_min threshold is always set slightly higher
    # than LMK minfree's last bin value for all targets. It is calculated as
    # vmpressure_file_min = (last bin - second last bin ) + last bin
    #
    # Set allocstall_threshold to 0 for all targets.
    #

    MemTotalStr=`cat /proc/meminfo | grep MemTotal`
    MemTotal=${MemTotalStr:16:8}

    # Read adj series and set adj threshold for PPR and ALMK.
    # This is required since adj values change from framework to framework.
    adj_series=`cat /sys/module/lowmemorykiller/parameters/adj`
    adj_1="${adj_series#*,}"
    set_almk_ppr_adj="${adj_1%%,*}"

    # PPR and ALMK should not act on HOME adj and below.
    # Normalized ADJ for HOME is 6. Hence multiply by 6
    # ADJ score represented as INT in LMK params, actual score can be in decimal
    # Hence add 6 considering a worst case of 0.9 conversion to INT (0.9*6).
    # For uLMK + Memcg, this will be set as 6 since adj is zero.
    set_almk_ppr_adj=$(((set_almk_ppr_adj * 6) + 6))
    echo $set_almk_ppr_adj > /sys/module/lowmemorykiller/parameters/adj_max_shift

    # Calculate vmpressure_file_min as below & set for 64 bit:
    # vmpressure_file_min = last_lmk_bin + (last_lmk_bin - last_but_one_lmk_bin)
    minfree_series=`cat /sys/module/lowmemorykiller/parameters/minfree`
    minfree_1="${minfree_series#*,}" ; rem_minfree_1="${minfree_1%%,*}"
    minfree_2="${minfree_1#*,}" ; rem_minfree_2="${minfree_2%%,*}"
    minfree_3="${minfree_2#*,}" ; rem_minfree_3="${minfree_3%%,*}"
    minfree_4="${minfree_3#*,}" ; rem_minfree_4="${minfree_4%%,*}"
    minfree_5="${minfree_4#*,}"

    vmpres_file_min=$((minfree_5 + (minfree_5 - rem_minfree_4)))
    echo $vmpres_file_min > /sys/module/lowmemorykiller/parameters/vmpressure_file_min
    if [ $MemTotal -lt 3145728 ]; then
        echo "18432,23040,27648,32256,100640,120640" > /sys/module/lowmemorykiller/parameters/minfree
    elif [ $MemTotal -lt 4194304 ]; then
        echo "18432,23040,27648,38708,120640,144768" > /sys/module/lowmemorykiller/parameters/minfree
    elif [ $MemTotal -lt 6291456 ]; then
        echo "18432,23040,27648,64512,165888,225792" > /sys/module/lowmemorykiller/parameters/minfree
    else
        echo "18432,23040,27648,96768,276480,362880" > /sys/module/lowmemorykiller/parameters/minfree
    fi

    # Enable adaptive LMK for all targets &
    # use Google default LMK series for all 64-bit targets >=2GB.
    echo 1 > /sys/module/lowmemorykiller/parameters/enable_adaptive_lmk

    # Enable oom_reaper
    if [ -f /sys/module/lowmemorykiller/parameters/oom_reaper ]; then
        echo 1 > /sys/module/lowmemorykiller/parameters/oom_reaper
    fi

    #Set PPR parameters for all other targets.
    echo $set_almk_ppr_adj > /sys/module/process_reclaim/parameters/min_score_adj
    echo 0 > /sys/module/process_reclaim/parameters/enable_process_reclaim
    echo 50 > /sys/module/process_reclaim/parameters/pressure_min
    echo 70 > /sys/module/process_reclaim/parameters/pressure_max
    echo 30 > /sys/module/process_reclaim/parameters/swap_opt_eff
    echo 512 > /sys/module/process_reclaim/parameters/per_swap_size
}

# Set Memory parameters
configure_memory_parameters
