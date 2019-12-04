<?php

$timeStep = 3600;
$hourStep = 1 * $timeStep;
$dayStep = 24 * $timeStep;

// sample/memory offsets are always in the past
$sampleOffsets = array (
    -1 * $dayStep,
    -2 * $dayStep,
    -3 * $dayStep,
    -6 * $dayStep,
    -7 * $dayStep,
    -8 * $dayStep,
    -9 * $dayStep,
    -10 * $dayStep,
    -13 * $dayStep,
    -14 * $dayStep,
    -15 * $dayStep,
    -16 * $dayStep,
    -17 * $dayStep,
);

$inputNames = array (
    "file",
    "time",
    "-1d",
    "-2d",
    "-3d",
    "-6d",
    "-7d",
    "-8d",
    "-9d",
    "-10d",
    "-13d",
    "-14d",
    "-15d",
    "-16d",
    "-17d",
    "action"
);

// reward offsets are always extracted from the future
$rewardOffset = -$sampleOffsets[0];
?>
