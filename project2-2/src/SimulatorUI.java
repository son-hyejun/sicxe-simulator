import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * GUI 구성 및 사용자 인터랙션 처리를 담당한다.
 * */
public class SimulatorUI extends JFrame {
    private final VisualSimulator visualSimulator;

    final int REGISTER_COUNT = 10;
    final String[] regNames = {"A (#0)", "X (#1)", "L (#2)", "B (#3)", "S (#4)", "T (#5)", "F (#6)", "", "PC (#8)", "SW (#9)"};

    // File Open
    JTextField fileNameField;
    JButton openButton;

    // Header. End
    JTextField programNameField, startAddrField, lengthField;
    JTextField firstInstructionAddrField;

    // Register
    JTextField[] regDecFields;
    JTextField[] regHexFields;

    // Simulator
    JTextField memStartAddrField, targetAddrField, deviceField;
    JTextPane memArea;
    JButton executeButton1Step, executeButtonAll, terminateButton;

    // Log
    JTextPane logTextArea;

    /**
     * 컴포넌트 초기화, 레이아웃 설정, 버튼 리스너를 등록한다.
     * */
    public SimulatorUI(VisualSimulator visualSimulator) {
        this.visualSimulator = visualSimulator;

        // 기본 설정
        setTitle("SIC/XE Simulator");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 전체 레이아웃
        JPanel mainPanel = new JPanel();

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // panels
        JPanel filePanel = buildFileSelectionPanel();
        JPanel headerPanel = buildHeaderPanel();
        JPanel endPanel = buildEndPanel();
        JPanel registerPanel = buildRegisterPanel();
        JPanel simulatorPanel = buildSimulatorPanel();
        JPanel logPanel = buildLogPanel();

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(filePanel)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(headerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(registerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(endPanel)
                                        .addComponent(simulatorPanel)))
                        .addComponent(logPanel)


        );



        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(filePanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(headerPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(registerPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(endPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(simulatorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(logPanel)
        );

        setContentPane(mainPanel);

        // 파일 열기 버튼 리스너 등록
        openButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            /*chooser.setFileFilter(new FileNameExtensionFilter("Obj File", "obj"));*/
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                fileNameField.setText(file.getName());
                visualSimulator.load(file);
            }
        });

        // 실행 버튼 리스너 등록
        executeButton1Step.addActionListener(e -> visualSimulator.oneStep());
        executeButtonAll.addActionListener(e -> visualSimulator.allStep());

        // 종료 버튼 리스너 등록
        terminateButton.addActionListener(e -> System.exit(0));

    }

    /**
     * 파일 선택과 관련된 패널을 생성한다.
     * */
    private JPanel buildFileSelectionPanel() {
        JPanel panel = new JPanel();
        JLabel filenameLabel = new JLabel("File Name : ");
        fileNameField = new JTextField(20);
        fileNameField.setEditable(false);
        openButton = new JButton("Open");

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(filenameLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fileNameField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(openButton, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(filenameLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fileNameField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(openButton, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        return panel;
    }

    /**
     * header record 관련 정보를 표시하는 패널을 생성한다.
     * */
    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel();
        JLabel programNameLabel = new JLabel("Program Name : ");
        JLabel startAddrLabel = new JLabel("<html>Start Address of<br>Object Program : </html>");
        JLabel lengthLabel = new JLabel("Length of Program : ");

        programNameField = new JTextField(10);
        programNameField.setEditable(false);
        startAddrField = new JTextField(10);
        startAddrField.setEditable(false);
        lengthField = new JTextField(10);
        lengthField.setEditable(false);

        panel.setBorder(BorderFactory.createTitledBorder("H (Header Record)"));
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(programNameLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(programNameField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(startAddrLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(startAddrField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lengthLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lengthField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(programNameLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(programNameField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(startAddrLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(startAddrField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lengthLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lengthField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
        );

        return panel;
    }

    /**
     * end record 관련 정보를 표시하는 패널을 생성한다.
     * */
    private JPanel buildEndPanel() {
        JPanel panel = new JPanel();

        JLabel firstInstructionAddrLabel = new JLabel("<html>Address of First instruction<br>in Object Program:</html>");

        firstInstructionAddrField = new JTextField(10);
        firstInstructionAddrField.setEditable(false);

        panel.setBorder(BorderFactory.createTitledBorder("E (End Record)"));

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(firstInstructionAddrLabel)
                        .addComponent(firstInstructionAddrField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(firstInstructionAddrLabel)
                        .addComponent(firstInstructionAddrField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        return panel;
    }

    /**
     * 레지스터 정보를 출력하는 패널을 생성한다.
     * */
    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel();

        JLabel[] labels = new JLabel[REGISTER_COUNT];
        regDecFields = new JTextField[REGISTER_COUNT];
        regHexFields = new JTextField[REGISTER_COUNT];

        for (int i = 0; i < REGISTER_COUNT; i++) {
            labels[i] = new JLabel(regNames[i]);
            regDecFields[i] = new JTextField(5);
            regHexFields[i] = new JTextField(5);
        }

        panel.setBorder(BorderFactory.createTitledBorder("Register"));

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        GroupLayout.ParallelGroup labelColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup decColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup hexColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

        JLabel empty = new JLabel();
        JLabel decLabel = new JLabel("DEC");
        JLabel hexLabel = new JLabel("HEX");

        labelColumn.addComponent(empty);
        decColumn.addComponent(decLabel);
        hexColumn.addComponent(hexLabel);

        for (int i = 0; i < REGISTER_COUNT; i++) {
            if (!(i == 7)) {
                labelColumn.addComponent(labels[i]);
                if (!(i == 9 || i == 6)) {
                    decColumn.addComponent(regDecFields[i]);
                }

                hexColumn.addComponent(regHexFields[i]);
            }
        }

        hGroup.addGroup(labelColumn)
                .addGroup(decColumn)
                .addGroup(hexColumn);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(empty)
                .addComponent(decLabel)
                .addComponent(hexLabel));

        for (int i = 0; i < REGISTER_COUNT; i++) {
            if (!(i == 7)) {
                if (!(i == 9 || i == 6)) {
                    vGroup.addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(labels[i])
                                    .addComponent(regDecFields[i])
                                    .addComponent(regHexFields[i])
                    );
                } else {
                    vGroup.addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(labels[i])
                                    .addComponent(regHexFields[i])
                    );
                }
            }


        }

        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);

        return panel;
    }

    /**
     * 실행 관련 정보를 표시하는 패널을 생성한다.
     * */
    private JPanel buildSimulatorPanel() {
        JPanel panel = new JPanel();

        // start address in memory
        JLabel startAddrLabel = new JLabel("Start Address in Memory");
        memStartAddrField = new JTextField(10);
        memStartAddrField.setPreferredSize(new Dimension(100, 25));
        memStartAddrField.setEditable(false);

        // target address
        JLabel targetAddrLabel = new JLabel("Target Address : ");
        targetAddrField = new JTextField(10);
        targetAddrField.setEditable(false);

        // memory
        JLabel memLabel = new JLabel("Memory : ");
        memArea = new JTextPane();
        memArea.setEditable(false);

        JScrollPane memScrollPane = new JScrollPane(memArea);
        memScrollPane.setPreferredSize(new Dimension(300, 100));
        memScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        memScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 사용중인 장치
        JLabel deviceLabel = new JLabel("사용중인 장치");
        deviceField = new JTextField(5);
        deviceField.setEditable(false);

        // buttons
        executeButton1Step = new JButton("실행(1step)");
        executeButtonAll = new JButton("실행 (all)");
        terminateButton = new JButton("종료");

        // layout
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(startAddrLabel)
                        .addComponent(memStartAddrField, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(targetAddrLabel)
                                .addComponent(targetAddrField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
                        .addComponent(memLabel)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(memScrollPane, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(deviceLabel)
                                        .addComponent(deviceField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                                        .addComponent(executeButton1Step, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                                        .addComponent(executeButtonAll, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                                        .addComponent(terminateButton, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
                        )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(startAddrLabel)
                        .addComponent(memStartAddrField, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(targetAddrLabel)
                                .addComponent(targetAddrField, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addComponent(memLabel)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(memScrollPane, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(deviceLabel)
                                        .addComponent(deviceField, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(executeButton1Step)
                                        .addComponent(executeButtonAll)
                                        .addComponent(terminateButton)))

        );

        return panel;
    }

    /**
     * 명령어 실행 로그를 출력하는 패널을 생성한다.
     * */
    private JPanel buildLogPanel() {
        JPanel panel = new JPanel();

        JLabel logLabel = new JLabel("Log (명령어 수행 관련) : ");
        logTextArea = new JTextPane();
        logTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(logLabel)
                        .addComponent(scrollPane)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(logLabel)
                        .addComponent(scrollPane)
        );

        return panel;
    }

}
