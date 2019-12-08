<?php

$timeStep = 3600;
$hourStep = 1 * $timeStep;
$dayStep = 24 * $timeStep;

// sample/memory offsets are always in the past
// Thursday
/*$sampleOffsets = array (
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
);*/
// Friday
$sampleOffsets = array (
    -1 * $dayStep,
    -2 * $dayStep,
    -3 * $dayStep,
    -4 * $dayStep,
    -7 * $dayStep,
//    -8 * $dayStep,
    -9 * $dayStep,
    -10 * $dayStep,
    -11 * $dayStep,
    -14 * $dayStep,
    -15 * $dayStep,
    -16 * $dayStep,
    -17 * $dayStep,
    -18 * $dayStep
);

$inputNames = array (
    "file",
    "time",
    "-1d",
    "-2d",
    "-3d",
    "-4d",
    "-7d",
//    "-8d",
    "-9d",
    "-10d",
    "-11d",
    "-14d",
    "-15d",
    "-16d",
    "-17d",
    "-18d",
    "action"
);

// reward offsets are always extracted from the future
$rewardOffset = -$sampleOffsets[2];

$marketWhitelist = array(
    "amzn.us",
    "msft.us",
    "fb.us",
    "intc.us",
    "csco.us",
    "googl.us",
    "aapl.us",
    "ebay.us"
);

?>
