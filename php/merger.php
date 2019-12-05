<?php
    include "config.php";

    $DIM = 2;

    $csv = file_get_contents("dataset/dataset.csv");
    $csv = trim($csv);
    $rows = explode("\n", $csv);
    $data = array();
    $startLine = true;

    for ($j = 1; $j < count($rows); ++$j) {
        $row = $rows[$j];
        $cols = explode(",", $row);

        $file = $cols[0];
        $time = $cols[1];

        $data[$time][$file] = $row;
    }

    $times = array_keys($data);

    for ($j = 0; $j < count($times); ++$j) {
        $companies = $data[$times[$j]];

        if (count($companies) === $DIM) {
            if ($startLine) {
                $companyNames = array_keys($companies);
                $headerArray = array();
                for ($i = 0; $i < count($companyNames); ++$i) {
                    $companyName = $companyNames[$i];
                    $template = implode(",", $inputNames);
                    $finalTemplate = str_replace(",", "@$companyName,", $template) . "@$companyName";
                    $headerArray[] = $finalTemplate;
                }
                file_put_contents("dataset/dataset-merge.csv", implode(",", $headerArray) . "\n", LOCK_EX);
                $startLine = false;
            }
            $companyRows = array_values($companies);
            $datasetLine = implode(',', $companyRows);
            file_put_contents("dataset/dataset-merge.csv", "$datasetLine\n", FILE_APPEND | LOCK_EX);
        }
    }

?>
