import org.hyperskill.hstest.dynamic.input.DynamicTestingMethod;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;

import static org.hyperskill.hstest.common.Utils.sleep;

public class Tests extends StageTest<String> {

    @DynamicTestingMethod
    CheckResult test() {
        final TestedProgram server = new TestedProgram("chat.server");
        final TestedProgram client1 = new TestedProgram("chat.client");
        final TestedProgram client2 = new TestedProgram("chat.client");
        final TestedProgram client3 = new TestedProgram("chat.client");
        client1.setReturnOutputAfterExecution(false);
        client2.setReturnOutputAfterExecution(false);
        client3.setReturnOutputAfterExecution(false);
        final String countIs = "Count is ";
        final int executePause = 50;

        server.startInBackground();

        //////Client 1

        client1.start();
        sleep(executePause);

        final String client1Start = client1.getOutput().trim();
        if (!"Client started!".equals(client1Start))
            return CheckResult.wrong("Can't get the \"Client started!\" message");

        client1.execute("1 2 3");
        sleep(executePause);

        final String client1Answer1 = client1.getOutput().trim();
        if (!(countIs + "3").equals(client1Answer1))
            return CheckResult.wrong("Client showed a wrong answer!");

        client1.execute("1 2");
        sleep(executePause);

        final String client1Answer2 = client1.getOutput().trim();
        if (!(countIs + "2").equals(client1Answer2))
            return CheckResult.wrong("Client showed a wrong answer!");

        client1.execute("/exit");
        sleep(executePause);

        //////Client 2

        client2.start();
        sleep(executePause);
        client2.getOutput();

        client2.execute("By my hands");
        sleep(executePause);

        final String client2Answer1 = client2.getOutput().trim();
        if (!(countIs + "3").equals(client2Answer1))
            return CheckResult.wrong("Client showed a wrong answer!");

        /////Client 3

        client3.start();
        sleep(executePause);
        client3.getOutput();

        client3.execute("Zzzz.");
        sleep(executePause);

        final String client3Answer1 = client3.getOutput().trim();
        if (!(countIs + "1").equals(client3Answer1))
            return CheckResult.wrong("Client showed a wrong answer!");

        client3.execute("want to sleep");
        sleep(executePause);

        final String client3Answer2 = client3.getOutput().trim();
        if (!(countIs + "3").equals(client3Answer2))
            return CheckResult.wrong("Client showed a wrong answer!");

        client3.execute("/exit");

        //////Client 2 AGAIN

        client2.execute("Repeat");
        sleep(executePause);

        final String client2Answer2 = client2.getOutput().trim();
        if (!(countIs + "1").equals(client2Answer2))
            return CheckResult.wrong("Client showed a wrong answer!");

        client2.execute("/exit");
        sleep(executePause);

        //////Server
		
		final String serverResult = server.getOutput().trim();

        if (!serverResult.matches(
        "Server started!\n" +

        "Client \\d+ connected!\n" +
        "Client \\d+ sent: 1 2 3\n" +
        "Sent to client \\d+: " + countIs + "3\n" +
        "Client \\d+ sent: 1 2\n" +
        "Sent to client \\d+: " + countIs + "2\n" +
        "Client \\d+ disconnected!\n" +

        "Client \\d+ connected!\n" +
        "Client \\d+ sent: By my hands\n" +
        "Sent to client \\d+: " + countIs + "3\n" +

        "Client \\d+ connected!\n" +
        "Client \\d+ sent: Zzzz.\n" +
        "Sent to client \\d+: " + countIs + "1\n" +

        "Client \\d+ sent: want to sleep\n" +
        "Sent to client \\d+: " + countIs + "3\n" +
        "Client \\d+ disconnected!\n" +

        "Client \\d+ sent: Repeat\n" +
        "Sent to client \\d+: " + countIs + "1\n" +
        "Client \\d+ disconnected!"
        ))
            return CheckResult.wrong(
            "Server showed wrong messages or messages in wrong order");
			//return CheckResult.wrong("======+=======\n" + serverResult + "\n======+=======\n");


        return CheckResult.correct();
    }

}
