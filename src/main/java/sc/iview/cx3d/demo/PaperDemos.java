package sc.iview.cx3d.demo;

import graphics.scenery.SceneryBase;
import io.scif.SCIFIOService;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.ops.OpService;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.ops.parse.token.Comma;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.scijava.Context;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.io.IOService;
import org.scijava.log.LogLevel;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;
import org.scijava.service.SciJavaService;
import org.scijava.thread.ThreadService;
import sc.fiji.snt.SNTService;
import sc.fiji.snt.Tree;
import sc.fiji.snt.analysis.TreeAnalyzer;
import sc.iview.SciView;
import sc.iview.SciViewService;
import sc.iview.commands.demo.ResourceLoader;
import sc.iview.cx3d.commands.RandomBranchingDemo;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PaperDemos {

    static public void main(String args[]) {
        SceneryBase.xinitThreads();

        System.setProperty( "scijava.log.level:sc.iview", "debug" );
        Context context = new Context( ImageJService.class, SciJavaService.class, SCIFIOService.class,
                ThreadService.class, ScriptService.class, LogService.class, SNTService.class);

        // For developer debugging
//        UIService ui = context.service( UIService.class );
//        if( !ui.isVisible() ) ui.showUI();

        IOService io = context.service( IOService.class );
        OpService ops = context.service( OpService.class );
        LogService log = context.service( LogService.class );
        SciViewService sciViewService = context.service( SciViewService.class );
        SciView sciView = sciViewService.getOrCreateActiveSciView();
        SNTService sntService = context.service( SNTService.class );

        log.setLevel(LogLevel.WARN);// Disable INFO logging

        CommandService commandService = context.service(CommandService.class);

        String[] header = new String[]{"tGrowth", "Speed", "pBifurcate", "pBranch", "numBranchPoints", "cableLength",
            "primaryLength"};

        double[] speeds = new double[]{100.0, 50.0, 200.0};
        double[] pBranch = new double[]{0.005, 0.0025, 0.01};
        //double[] pBifurcate = new double[]{0.005, 0.0025, 0.01};
        //double[] pBifurcate = new double[]{0.005, 0.005, 0.005};
        double[] pBifurcate = new double[]{0.005, 0.0045, 0.004, 0.0035, 0.003, 0.0025, 0.002, 0.0015, 0.001, 0.0005 };

        int tGrowth = 3;
        String resultDir = "/home/kharrington/Data/SNT_Paper/demo001/";

        for( String el : header ) System.out.print(el + "\t");
        System.out.println();

        sntService.initialize(true);

        for( int s = 0; s < speeds.length; s++ ) {
            for( int br = 0; br < pBranch.length; br++ ) {
                for( int bi = 0; bi < pBifurcate.length; bi++ ) {

                    sciView.reset();

//                    if( bi == 1 ) {
//                        try {
//                            Thread.sleep(10000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }

                    Tree myTree = new Tree();

                    HashMap<String, Object> argmap = new HashMap<>();
                    argmap.put("context", context);
                    argmap.put("sntService", sntService);
                    argmap.put("sciView", sciView);
                    argmap.put("maxTime", tGrowth);
                    argmap.put("speed", speeds[s]);
                    argmap.put("probabilityToBifurcate",pBifurcate[bi]);
                    argmap.put("probabilityToBranch", pBranch[br]);
                    argmap.put("tree", myTree);

                    Future<CommandModule> result = commandService.run(RandomBranchingDemo.class, true, argmap);

                    while( !result.isDone() ) {
                    //while(true) {
//                        try {
//                            if (result.get().isOutputResolved("tree")) break;
//                        } catch (InterruptedException | ExecutionException e) {
//                            e.printStackTrace();
//                        }
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


//                    Tree tree = null;
//                    try {
//                        tree = (Tree) result.get().getOutput("tree");
//                    } catch (InterruptedException | ExecutionException e) {
//                        e.printStackTrace();
//                    }

//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }


                    //sciView.takeScreenshot( resultDir + "screenshot_speed_" + speeds[s] + "_pBifurcate_" + pBifurcate[bi] + "_pBranch_" + pBranch[br] + ".png");

//
//                    Img<UnsignedByteType> screenshot = sciView.getScreenshot();
//                    try {
//                        io.save(screenshot, resultDir + "screenshot_speed_" + speeds[s] + "_pBifurcate_" + pBifurcate[bi] + "_pBranch_" + pBranch[br] + ".png");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    //TreeAnalyzer analyzer = sntService.getAnalyzer(false);

                    TreeAnalyzer analyzer = new TreeAnalyzer(myTree);

                    String[] row = new String[]{String.valueOf(tGrowth), String.valueOf(speeds[s]),
                            String.valueOf(pBifurcate[bi]), String.valueOf(pBranch[br]),
                            String.valueOf(analyzer.getBranchPoints().size()),
                            String.valueOf(analyzer.getCableLength()),
                            String.valueOf(analyzer.getPrimaryLength())};

                    for( String el : row ) System.out.print(el + "\t");
                    System.out.println();
                }
            }
        }

    }

}
