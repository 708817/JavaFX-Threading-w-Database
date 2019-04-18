/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafx_threadtest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Dido
 */
public class JavaFX_ThreadTest extends Application  {
    
//      IGNORE: If you want to change a variable whilst being used by a Thread, the 
//      variable must have a 'static volatile' declaration. 
//      Example: static volatile String string = ""; 
    
//      Regarding sa information details (e.g. Name, Address, Orders), see REF 5
//      Regarding sa connectivity ng database including its user and pass, see REF 1
    
//      Lahat ng may REF number, Ctrl + F niyo nalang para mahanap kung ano
//      yung tinutukoy.
    
    // MySQL database variables
    Connection con; // REF 1
    Statement st; // REF 2
    ResultSet rs; // REF 2
    
    // JavaFX GUI variables
    VBox vbb; // REF 3 Eto pangdisplay lang sa buong List ng gpList
    List<GridPane> gpList; // REF 4 Eto lalagyan or lagayan ng mga GridPane. Each GridPane contains the Information Details mentioned above.
    Button btnAccept;
    Button btnDecline;
    
    @Override
    public void start(Stage primaryStage) {
        
        // The ff. declarations will be modified by a Thread.
        // Default GUI display declarations START
        vbb = new VBox(); // REF 3

        StackPane root = new StackPane();
        root.getChildren().add(vbb);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
        // Default GUI display declarations END

        // Creating Connectivity to database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); 
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?autoReconnect=true&useSSL=false", "root", "12345"); // REF 1
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        startThread();
    }

    public void startThread() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                while (true) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            
                        // LAHAT NG KAILANGAN NAG-UUPDATE SA GUI FOR EVERY FIVE SECONDS, LAGAY DITO
                            
                            // For every instance, i-clear yung laman ng Vbox 
                            vbb.getChildren().clear();
                            // Declaration of new GridPane List
                            gpList = new ArrayList<>(); // REF 4
                            
                            try {
                                // REF 2 
                                st = con.createStatement();
                                rs = st.executeQuery("SELECT * FROM test_table");
                                
                                while (rs.next()) {
                                    
                                    // Dito nilalagay ang declarations ng mga information details,
                                    // so kung gusto niyo lagyan ng Address, declare nalang ng Label para
                                    // ilagay doon. REF 5
                                    // NOTE: dapat yung pangalan sa loob ng rs.getString(); may parehas doon
                                    // sa MySQL Database.
                                    Label lblString = new Label(rs.getString("string"));
                                    Label lblData = new Label(Integer.toString(rs.getInt("data")));
                                    
                                    // Dito finoformat yung layout ng information detail ng isang customer.
                                    // Gamit ko GridPane since akala ko eto yung pinakamadaling i-format.
                                    GridPane tempGP = new GridPane();
                                    tempGP.add(lblString, 0, 0);
                                    tempGP.add(lblData, 0, 1);
                                    tempGP.add(btnAccept = new Button("Accept"), 1, 0);
                                    tempGP.add(btnDecline = new Button("Decline"), 2, 0);
                                    
                                    // Event Handling ng mga interactive GUIs. For every customer, may
                                    // sarili silang event handling, pero same yung methods.
                                    // Interactive GUIs START
                                    btnAccept.setOnAction(e -> {
                                        System.out.println("You pressed Accept for " 
                                                + lblString.getText() + ", " 
                                                + lblData.getText());
                                    });
                                    
                                    btnDecline.setOnAction(e -> {
                                        System.out.println("You pressed Decline for " 
                                                + lblString.getText() + ", " 
                                                + lblData.getText());
                                    });
                                    // Interactive GUIs END
                                    
                                    // After setting up the format for that one customer, we add it to the GridPane List 
                                    gpList.add(tempGP);
                                }
                                
                                // After placing the format in a GridPane List, display all its contents in the VBox
                                vbb.getChildren().addAll(gpList);
                                // I-clear mo na para sa susunod na instance
                                gpList.clear();

                                rs.close();
                                st.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    // Stop Refreshing for Five Seconds. Bumabagal PC kapag tuloy-tuloy siya.
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        };
        
        // Declaration of thread
        Thread th = new Thread(task);
        // KAILANGAN ITO PARA KAPAG CINLOSE MO YUNG APP, MAGSASARA RIN YUNG THREAD
        th.setDaemon(true);
        // Start na si thread.
        th.start();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
