<?php
    include "config.php";

    $marketWhitelist = array(
        "amzn.us",
        "googl.us"
    );

    for ($i = 0; $i < count($marketWhitelist); ++$i) {
        $file = explode(".", $marketWhitelist[$i])[0];
        $rowMap = array();

        $json = file_get_contents("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=$file&apikey=VEFJBD6FTNV1RAYE");
        $json = json_decode($json, true);
        $rows = $json["Time Series (Daily)"];
        //$today = gmdate("Y-m-d");
        $today = $argv[1];
        $utime = strtotime($today);
        $today = gmdate("Y-m-d", $utime);

        $pivotPrice = getQuote($rows, $today);

        for ($j = 0; $j < count($sampleOffsets); ++$j) {
            $time = $utime + $sampleOffsets[$j];
            $prevDate = gmdate("Y-m-d", $time);
            $price = getQuote($rows, $prevDate);
            //echo "TODAY=$prevDate, UTIME=$time, PRICE=$price\n";
            if ($price !== '?') {
                $variation = getVariation($pivotPrice, $price);
            } else {
                $variation = '?';
            }
            echo $prevDate." --> ".$inputNames[2+$j]."@".$marketWhitelist[$i]."=".$variation."\n";
        }
    }

    function getVariation($pivot, $price) {
        return round(($pivot - $price) * 100 / $pivot, 2);
    }

    function getQuote($rows, $dateStr) {
        if (!isset($rows[$dateStr])) {
            return '?';
        }
        return $rows[$dateStr]["1. open"];
    }

?>
