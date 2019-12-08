<?php
    include "config.php";

    $payload = array();

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
            $operatorId = $inputNames[2+$j]."@".$marketWhitelist[$i].".txt";
            //echo $prevDate." --> ".$operatorId."=".$variation."\n";
            if ($variation == 0) {
                $payload[$operatorId]["0.0"] = true;
            } else {
                $payload[$operatorId]["$variation"] = true;
            }
        }
    }

    echo json_encode($payload);

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
