<?php
    include "config.php";

    $dir    = 'data';
    $files  = scandir($dir);
    $fileCount = count($files);
    $blacklist = array();

    $ROW_CAP = 10000000;

    $marketWhitelist = array(
        "amzn.us"
    );

    $datasetQueryValues = [];

    file_put_contents("dataset/dataset.csv", implode(",", $inputNames)."\n", LOCK_EX);

    for ($i = 0; $i < count($files); ++$i) {
        $file = $files[$i];
        $rowMap = array();

        if ($file === '.' ||
            $file === '..' ||
            strrpos($file, ".txt") === FALSE) {
            continue;
        }

        if (!isMarketWhitelist($file, $marketWhitelist)) {
            continue;
        }

        $csv = file_get_contents("$dir/$file");
        $csv = trim($csv);

        if (strrpos($csv, "\n") !== FALSE) {
            $rows = explode("\n", $csv);
            $rowCount = count($rows);
            $lastTime = -1;

            for ($j = 1; $j < min($rowCount, $ROW_CAP); ++$j) {
                $row = $rows[$j];
                $cols = explode(",", $row);

                if (count($cols) > 5) {
                    list($year, $month, $day) = sscanf($cols[0], "%04d%02d%02d");
                    $time = strtotime("$day-$month-$year");

                    $diff = $time - $lastTime;
                    $rowMap[$time] = $cols;

                    /*if ($j > 1 && $diff != 3600000) {
                        //echo "$file @ row $j: $diff\n";
                        $blacklist[$file] = true;
                    }*/

                    $diffPercOpen = [];
                    $diffPercVolume = [];

                    for ($k = 0; $k < count($sampleOffsets); ++$k) {
                        $diffPercOpen[] = processOffset($rowMap, $time, $sampleOffsets[$k], 1);
                        $diffPercVolume[] = processOffset($rowMap, $time, $sampleOffsets[$k], 5);
                    }

                    preProcessRow($rowMap, $rows, $j, $time + $rewardOffset);
                    $rewardDiffPercOpen = processOffset($rowMap, $time + $rewardOffset, -$rewardOffset, 1);

                    echo implode(',', $diffPercOpen)."\n";
                    echo implode(',', $diffPercVolume)."\n";
                    echo $rewardDiffPercOpen."%   action? $action\n\n";

                    $action = $rewardDiffPercOpen;

                    $market = explode(".", $file)[0];

                    $datasetLine = "$file,$time," . implode(',', $diffPercOpen) /* . "," . implode(',', $diffPercVolume) */ . ",$action\n";

                    if (array_search('?', $diffPercOpen) === FALSE && $action !== '?') {
                        file_put_contents("dataset/dataset.csv", $datasetLine, FILE_APPEND | LOCK_EX);
                    }

                    $lastTime = $time;
                }
                else {
                    // echo "$file @ $j has malformed line(s): $row\n";
                    $blacklist[$file] = true;
                }
            }

        } else {
            // echo "$file is empty.\n";
            $blacklist[$file] = true;
        }
    }

    function processOffset ($rowMap, $time, $sampleOffset, $colId) {
        $openNow = $rowMap[$time][$colId];
        $openWithOffset = $rowMap[$time + $sampleOffset][$colId];

        if (isset($openWithOffset) && $openNow > 0) {
            $diffPerc = round(($openNow - $openWithOffset) * 100.0 / $openNow, 2);
        } else {
            $diffPerc = '?';
        }

        return $diffPerc;
    }

    function preProcessRow(&$rowMap, $rows, $seedIndex, $targetTime) {
        if (isset($rowMap[$targetTime])) {
            return $rowMap[$targetTime];
        }
        for ($i = $seedIndex + 1; $i < count($rows); ++$i) {
            $row = $rows[$i];
            $cols = explode(",", $row);
            if (count($cols) > 5) {
                list($year, $month, $day) = sscanf($cols[0], "%04d%02d%02d");
                $time = strtotime("$day-$month-$year");
                $rowMap[$time] = $cols;
                if ($time === $targetTime) {
                    return $cols;
                }
            }
        }
        return null;
    }

    function isMarketWhitelist($file, $whitelist) {
        for ($i = 0; $i < count($whitelist); ++$i) {
            if (strrpos($file, $whitelist[$i]) !== FALSE) {
                return true;
            }
        }
        return false;
    }

?>
