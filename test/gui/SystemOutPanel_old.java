
package gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.awt.*;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import javax.swing.*;


/**
 *
 * @author  u56124
 */
public class SystemOutPanel_old extends javax.swing.JPanel {

    // Variables declaration - do not modify
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel panel_SystemLog;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textArea_log;
    PrintStream aPrintStream = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));
    // End of variables declaration

    public SystemOutPanel_old() throws IOException {

        panel_SystemLog = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea_log = new javax.swing.JTextArea();

        lblTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        lblTitle.setText("Processing output...");
        textArea_log.setColumns(20);
        textArea_log.setRows(5);
        jScrollPane1.setViewportView(textArea_log);

        System.setOut(aPrintStream);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel_SystemLog);
        panel_SystemLog.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle)
                .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE))))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(72, Short.MAX_VALUE))
                );


        this.add(panel_SystemLog);
        this.setVisible(true);
        
    }

   
    public static void showSystemOutPanel() throws IOException{
//        gui.MOAPIPanel.pnl_Content = new SystemOutPanel_old();
//        gui.MOAPIPanel.scrl_Content.setViewportView(gui.MOAPIPanel.pnl_Content);
    }


    class FilteredStream extends FilterOutputStream {

        public FilteredStream(OutputStream aStream) {
            super(aStream);
          }

        public void write(byte b[]) throws IOException {
            String aString = new String(b);
            textArea_log.append(aString);
        }

        public void write(byte b[], int off, int len) throws IOException {
            String aString = new String(b , off , len);
            textArea_log.append(aString);

            //Write to a text file
//            if (logFile) {
//                FileWriter aWriter = new FileWriter(fileName, true);
//                aWriter.write(aString);
//                aWriter.close();
//            }
        }
    }


}



