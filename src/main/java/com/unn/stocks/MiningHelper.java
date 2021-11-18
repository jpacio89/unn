package com.unn.stocks;

import com.unn.common.mining.MiningReport;
import com.unn.common.operations.AgentRole;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.session.actions.MineAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MiningHelper {
    public static Session mine(OuterDataset outerDataset, String target) {
        Context context = new Context();
        AgentRole role = new AgentRole();
        Session session = new Session(context, role);
        session.setOuterDataset(outerDataset);

        MineAction action = new MineAction();
        action.setSession(session);
        action.setConf(new JobConfig(target, new ArrayList<>()));

        action.act();

        MiningReport report = action.getSession().getReport();

        if (report == null || report.getConfusionMatrixes().size() == 0) {
            System.out.println("No reports to showcase");
        } else {
            System.out.println(String.format("Report statistics:%n%s", report.toString()));
        }

        return session;
    }

    public static void writeReportToFile(File inputFolder, Session session) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(
                    String.format("%s/report.log", inputFolder.getAbsolutePath())));
            writer.write(session.getReport().toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
