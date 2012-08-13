/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package global;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
// * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SysCommandExecutor{
    private static Process pr = null;
    
    public static void sysAnalyserCommandPost(String command){
//        gui.AnalyserTab.textArea_commandLine.setText(command);
    }
    
    public static void sysAnalyserCommandExecute(){
        try {
//            String commandLine = gui.AnalyserTab.textArea_commandLine.getText();
//            gui.AnalyserTab.textArea_cliResult.append("Command to be executed:\n"+ commandLine +"\n"+"\n");
//
//            StringBuffer result=sysCommandExecute(commandLine);
//            gui.AnalyserTab.textArea_cliResult.append(result.toString());

        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
   }
    
    public static StringBuffer sysCommandExecute(String cmd){
        StringBuffer result= new StringBuffer();
        try {
            Runtime rt = Runtime.getRuntime();
            
            if (constants.cGlobal.OSNAME.equals("Linux")){
                pr = rt.exec(cmd);
            }
            else if (constants.cGlobal.OSNAME.contains("Windows")){
                pr = rt.exec("cmd /c " + cmd);
            }
            else{
                //JOptionPane.showMessageDialog(gui.StartGUI.getFrames()[0], "Sorry, your Operating System is not supported by our software.\nSupported platforms include Windows and Linux.");
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;
            while((line=input.readLine()) != null) {
               result.append(line+"\n");
            }

            int exitVal = pr.waitFor();
            result.append("\n"+exitVal+"\n");

        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return result;
   }
    
}
