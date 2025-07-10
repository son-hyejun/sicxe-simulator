import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다. 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트
 * 하는 역할을 수행한다.
 *
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator {
    ResourceManager resourceManager = new ResourceManager();
    SicLoader sicLoader = new SicLoader(resourceManager);
    SicSimulator sicSimulator = new SicSimulator(resourceManager);
    private SimulatorUI ui;

    boolean isLoaded; // 프로그램 로드 여부

    /**
     * UI 컴포넌트를 연결한다.
     * */
    public void setUI(SimulatorUI ui) {
        this.ui = ui;
        isLoaded = false;

        // 실행 버튼 비활성화
        ui.executeButton1Step.setEnabled(false);
        ui.executeButtonAll.setEnabled(false);
    }

    /**
     * 프로그램 로드 명령을 전달한다.
     */
    public void load(File program) {
        // ...
        sicLoader.load(program);
        sicSimulator.load(program);
        isLoaded = true;
        update();
    };

    /**
     * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
     */
    public void oneStep() {
        sicSimulator.oneStep();
        update();
    };

    /**
     * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
     */
    public void allStep() {
        sicSimulator.allStep();
        update();
    };

    /**
     * 화면을 최신값으로 갱신하는 역할을 수행한다.
     */
    public void update() {
        if (ui == null) return;
        // 버튼 활성화
        if (isLoaded) {
            ui.executeButton1Step.setEnabled(true);
            ui.executeButtonAll.setEnabled(true);
        }

        // header 갱신
        String[] csInfo = resourceManager.csInfos.get(sicSimulator.curCs);
        ui.programNameField.setText(csInfo[0]);
        ui.startAddrField.setText(csInfo[1]);
        ui.lengthField.setText(csInfo[2]);

        // end 갱신
        ui.firstInstructionAddrField.setText(csInfo[1]);

        // register 갱신
        for (int i = 0; i < ui.regHexFields.length; i++) {
            int value = resourceManager.getRegister(i);

            ui.regHexFields[i].setText(String.format("%06X", value));

            // F, SW는 10진수 출력 제외
            if (!(i == 6 || i == 9)) {
                ui.regDecFields[i].setText(String.valueOf(value));
            }
        }

        // program startAddr
        ui.memStartAddrField.setText(String.format("%06X", resourceManager.memStartAddr));

        // target address
        ui.targetAddrField.setText(String.format("%X", sicSimulator.targetAddr));

        // current device
        ui.deviceField.setText(sicSimulator.useDev ? sicSimulator.curDev : "");

        // memory
        buildMemoryStyledView(ui.memArea, sicSimulator.prevAddr, sicSimulator.prevLen);

        // log 갱신
        updateLog(sicSimulator.getLogs());
    };

    /**
     * 메모리 전체를 표시한다.
     * */
    public void buildMemoryStyledView(JTextPane pane, int highlightStart, int highLightLen) {
        StyledDocument doc = pane.getStyledDocument();

        // 일반 스타일
        SimpleAttributeSet normalStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(normalStyle, "Monospaced");

        // 강조 스타일
        SimpleAttributeSet highlightStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(highlightStyle, "Monospaced");
        StyleConstants.setBackground(highlightStyle, Color.yellow);

        pane.setText("");

        int caretOffset = -1;

        // 실행 중인 명령어 강조
        // 주소 범위: highlightStart ~ highlightStart + highlightLen
        try {
            for (int addr = 0; addr < resourceManager.memory.length; addr++) {
                if (addr % 16 == 0) {
                    if (addr != 0) {
                        doc.insertString(doc.getLength(), "\n", normalStyle);
                    }

                    doc.insertString(doc.getLength(), String.format("%04X: ", addr), normalStyle);
                }

                byte b = resourceManager.readByte(addr);
                String hex = String.format("%02X", b);

                if (addr >= highlightStart && addr < highlightStart + highLightLen) {
                    caretOffset = doc.getLength();
                    doc.insertString(doc.getLength(), hex, highlightStyle);

                } else {
                    doc.insertString(doc.getLength(), hex, normalStyle);
                }
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

        // 커서 이동
        pane.setCaretPosition(Math.max(caretOffset, 0));
    }

    /**
     * 명령어 실행 로그를 표시한다.
     * */
    public void updateLog(List<String> logs) {
        StringBuilder logsBuilder = new StringBuilder();
        for (String log : logs) {
            logsBuilder.append(log);
        }

        ui.logTextArea.setText(logsBuilder.toString());
        ui.logTextArea.setCaretPosition(ui.logTextArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            VisualSimulator visualSimulator = new VisualSimulator();
            SimulatorUI ui = new SimulatorUI(visualSimulator);
            visualSimulator.setUI(ui);
            ui.setVisible(true);
        });
    }
}
