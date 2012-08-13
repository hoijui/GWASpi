/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

//imports
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.io.PrintStream;
class StreamToTextArea extends JFrame {

    //declare PrintStream and JTextArea
    private static PrintStream ps = null;
    static JTextArea outputTxtA = new JTextArea();

    static OutputStream out = new OutputStream()
    {
        public void write(int b) throws IOException {
            outputTxtA.append(String.valueOf((char) b));
        }

        public void write(byte[] b, int off, int len) {
            outputTxtA.append(new String(b, off, len));
        }
    };




    //constructor
    public StreamToTextArea() {

        setSize( 310, 180 );

        getContentPane().add(outputTxtA);

        //this is the trick: overload the println(String)
        //method of the PrintStream
        //and redirect anything sent to this to the text box
        ps =  new PrintStream(System.out) {
            public void println(String x) {
                outputTxtA.append(x + "\n");
            }
        };
    }

    public PrintStream getPs() {
        return ps;
    }

    public static void main(String args[]) {
        //create object
        StreamToTextArea blah = new StreamToTextArea();
        //show it
        blah.show();
        //redirect the output stream
        System.setOut(new PrintStream(out, true));

        //print to the text box
        System.out.println("IT'S ALIVE!!");
        //print to the terminal (not a string)
        System.out.println(1);
        //print the same thing to the text box (now a string)
        System.out.println("" + 2);
    }

}
