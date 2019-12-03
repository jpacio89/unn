<?php

$timeStep = 3600;
$hourStep = 1 * $timeStep;
$dayStep = 24 * $timeStep;

// sample/memory offsets are always in the past
$sampleOffsets = array (
//    -1  * $hourStep,
//    -4  * $hourStep,
//    -8  * $hourStep,
//    -12 * $hourStep,
//    -16 * $hourStep,
//    -20 * $hourStep,
    -24 * $hourStep,
    -2  * $dayStep,
    -3  * $dayStep,
//    -4  * $dayStep,
//    -5  * $dayStep,
//    -6  * $dayStep,
//    -7  * $dayStep,
//    -10  * $dayStep,
//    -13  * $dayStep,
//    -16  * $dayStep,
//    -19  * $dayStep,
//    -22  * $dayStep,
//    -25  * $dayStep,
//    -28  * $dayStep,
//    -31  * $dayStep
);

$inputNames = array (
    "file",
    "time",
//    "-1h",
//    "-4h",
//    "-8h",
//    "-12h",
//    "-16h",
//    "-20h",
    "-24h",
    "-2d",
    "-3d",
//    "-4d",
//    "-5d",
//    "-6d",
//    "-7d",
//    "-10d",
//    "-13d",
//    "-16d",
//    "-19d",
//    "-22d",
//    "-25d",
//    "-28d",
//    "-31d",
    "action"
);

// reward offsets are always extracted from the future
$rewardOffset = -$sampleOffsets[0];
?>
