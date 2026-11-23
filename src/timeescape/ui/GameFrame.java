package timeescape.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import timeescape.core.GameState;
import timeescape.model.Era;
import timeescape.system.GameAudio;

/** 화면 전체를 관리하는 JFrame. 좌우 회전 화살표와 시간 선택 버튼은 없다. */
public final class GameFrame extends JFrame {
    private static final String MENU = "menu";
    private static final String GAME = "game";
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JPanel gameRoot = new JPanel(new BorderLayout());
    private MainMenuPanel mainMenu;
    private boolean gameStarted;
    private final timeescape.system.SaveSlots saveSlots = new timeescape.system.SaveSlots(
        timeescape.system.SaveStore.defaultDirectory(), timeescape.core.ProgressCatalog.standard());
    private final timeescape.system.SaveStore saves = saveSlots.store(timeescape.system.SaveSlots.Slot.AUTO);
    private GameMenuOverlay continueSlots;
    private GameState.Snapshot lastSaved;
    private boolean replaceSave;
    private GameState state = new GameState();
    private boolean spareSelected;
    private final GameScenePanel scene = new GameScenePanel(this::interactScene);
    private final InventoryPanel inventory = new InventoryPanel(this::interactInventory);
    private final JLabel sceneTitle = new JLabel();
    private final MissionPanel missions = new MissionPanel();
    private final JLabel progress = new JLabel();
    private final JTextArea message = new JTextArea();
    private GameHud hud;

    public GameFrame() {
        super("TIME ESCAPE");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 650));
        setSize(1500, 880);
        setLocationRelativeTo(null);
        initUI();
        initMainMenu();
        setContentPane(cards);
        cardLayout.show(cards, MENU);
        refresh();
        javax.swing.SwingUtilities.invokeLater(() -> GameAudio.get().menu());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
            }
        });
    }

    @Override public void dispose() {
        if (!saveProgress()) return;
        GameAudio.get().shutdown();
        super.dispose();
    }

    private void showContinueSlots() {
        if (!saveProgress()) return;
        scene.setInputBlocked(true);
        continueSlots.open("load");
    }

    private void closeContinueSlots() {
        continueSlots.release();
        scene.setInputBlocked(false);
        if (mainMenu.isShowing()) mainMenu.requestFocusInWindow(); else scene.requestFocusInWindow();
    }

    private boolean loadSlot(timeescape.system.SaveSlots.Slot slot) {
        try {
            var result = saveSlots.load(slot);
            state = result.state();
            gameStarted = true;
            spareSelected = false;
            replaceSave = false;
            lastSaved = slot == timeescape.system.SaveSlots.Slot.AUTO && !result.recoveredFromBackup()
                ? state.snapshot() : null;
            scene.setEra(state.era());
            scene.restorePowered(state.powered());
            log(slot.title() + "에서 진행을 불러왔습니다.");
            refresh();
            cardLayout.show(cards, GAME);
            GameAudio.get().era(state.era());
            if (result.recoveredFromBackup()) JOptionPane.showMessageDialog(this,
                "선택한 저장소의 이전 백업으로 복구했습니다.", "저장 복구", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "선택한 저장소를 불러오지 못했습니다. 기존 파일과 현재 진행은 유지됩니다.",
                "불러오기 실패", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    private boolean saveManualSlot(timeescape.system.SaveSlots.Slot slot) {
        if (!gameStarted) return false;
        try {
            saveSlots.saveManual(slot, state.snapshot());
            mainMenu.setContinueAvailable(true);
            return true;
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "저장하지 못했습니다. 저장 위치의 쓰기 권한과 남은 공간을 확인해주세요.",
                "저장 실패", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    private boolean saveProgress() {
        if (!gameStarted) return true;
        var snapshot = state.snapshot();
        if (snapshot.equals(lastSaved)) return true;
        try {
            if (replaceSave) saves.startNew(snapshot); else saves.save(snapshot);
            replaceSave = false;
            lastSaved = snapshot;
            return true;
        } catch (java.io.IOException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "진행 상황을 저장하지 못했습니다.\n" + saves.file()
                + "\n저장 위치의 쓰기 권한과 남은 공간을 확인해주세요. 현재 게임은 계속 열려 있습니다.",
                "저장 실패", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    private void initUI() {
        hud = new GameHud(scene, missions, inventory, this::showHelp, this::resetGame, this::showMainMenu);
        hud.setTransitionSettings(()->mainMenu.isTransitionAnimationEnabled(),value->mainMenu.setTransitionAnimation(value));
        hud.setSaveSlots(saveSlots::summaries, this::saveManualSlot, this::loadSlot);
        gameRoot.add(hud, BorderLayout.CENTER);
    }
    private void showHelp() { hud.openHelp(); }
    private void resetGame() {
        state = new GameState(); lastSaved = null; replaceSave = true; scene.restorePowered(false); spareSelected = false; hud.closeNotebook();
        log("어두운 연구실입니다. 벽면 철제 배전함에서 전력을 복구하세요.");
        refresh(); GameAudio.get().play("ui_click");
    }

    private void initMainMenu() {
        mainMenu = new MainMenuPanel(new MainMenuPanel.MenuActions() {
            @Override public boolean confirmNewGame() {
                if (!saves.exists()) return true;
                scene.setInputBlocked(true);
                continueSlots.open("newgame");
                return false;
            }
            @Override public void startGame() {
                state = new GameState();
                lastSaved = null;
                replaceSave = true;
                scene.restorePowered(false);
                spareSelected = false;
                gameStarted = true;
                mainMenu.setContinueAvailable(true);
                log("어두운 연구실입니다. 벽면 철제 배전함에서 전력을 복구하세요.");
                refresh();
                cardLayout.show(cards, GAME);
                GameAudio.get().era(state.era());
            }
            @Override public void continueGame() {
                showContinueSlots();
            }
            @Override public void showSettings() {
                JCheckBox animation = new JCheckBox("게임 시작 시 소등·시계 파손 연출 사용", mainMenu.isTransitionAnimationEnabled());
                int result = JOptionPane.showConfirmDialog(GameFrame.this, animation, "메뉴 설정", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) mainMenu.setTransitionAnimation(animation.isSelected());
            }
            @Override public void exitGame() {
                int result = JOptionPane.showConfirmDialog(GameFrame.this, "종료하시겠습니까?", "게임 종료", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) dispose();
            }
        });
        mainMenu.setContinueAvailable(saveSlots.hasAny());
        continueSlots = new GameMenuOverlay(mainMenu, this::closeContinueSlots, ()->{}, ()->{});
        continueSlots.setSaveSlots(saveSlots::summaries, slot->false, this::loadSlot);
        continueSlots.setNewGameAction(mainMenu::startConfirmedNewGame);
        setGlassPane(continueSlots);
        cards.add(mainMenu, MENU);
        cards.add(gameRoot, GAME);
    }

    private void showMainMenu() {
        hud.closeNotebook();
        mainMenu.setContinueAvailable(saveSlots.hasAny());
        cardLayout.show(cards, MENU);
        GameAudio.get().menu();
    }

    private void refresh() {
        Era era = state.era();
        scene.setEra(era);
        scene.setPowered(state.powered());
        missions.refresh(state);
        hud.setLocation(state.era().title(), state.locationTitle().replaceFirst("^.*? · ", ""));
        progress.setText("    " + stageText());
        inventory.refresh(state, spareSelected);
        saveProgress();
    }

    private String stageText() {
        if (state.isEscaped()) return "탈출 완료";
        if (state.era() == Era.PRESENT) return state.canDepart() ? "두 미션 완료 · 과거 이동 가능" : state.powered() ? "출발 준비 · 두 미션을 자유롭게 진행" : "벽면 철제 배전함에서 전력 복구 필요";
        if (state.era() == Era.PAST) return state.isWatchRepaired() ? "장치 복구 완료 · 미래 이동 가능" : "대체 부품으로 장치 수리 필요";
        return state.isCoreInstalled() ? "최종 좌표 입력 가능" : "미래 핵심 부품 확보 및 수리";
    }

    private void log(String text) { if (hud != null) hud.showMessage(text); }

    private void interactScene(String id) {
        if (state.era() == Era.PRESENT && !state.powered() && !id.equals("POWER")) {
            log("전력이 없습니다. 벽면 철제 배전함을 먼저 조사하세요.");
            return;
        }
        var item = state.catalog().items().get(id);
        if (item != null && state.roomId().equals(item.room())) {
            state.collect(id);
            log("아이템을 확인했습니다. 연구 노트의 보유 아이템과 현재 목표를 확인하세요.");
            refresh();
            return;
        }
        var pending = state.nextObjectStep(id);
        if (pending.isEmpty()) {
            log(switch (id) {
                case "POWER" -> "전력이 복구되어 있습니다.";
                case "MEMO" -> "연구 계정 암호 해독 완료. 컴퓨터를 조사하세요.";
                case "PRESENT_PC" -> "USB 데이터 복사가 완료되었습니다. " + state.deviceNext();
                case "WORKBENCH" -> "장치 제작 완료. " + state.usbNext();
                case "PAST_SPARE" -> "황동 장치: 수리에 사용할 만한 부품은 이미 챙겼습니다.";
                case "FUTURE_CORE" -> "시간 실험 장치: 필요한 부품을 이미 확보했습니다.";
                default -> "조사할 수 없는 대상입니다.";
            });
            return;
        }
        var step = pending.get();
        if (!state.available(step.id()) || step.automatic()) {
            if (!step.mission().isBlank()) log(state.missionNext(step.mission()));
            else log(step.prompt().isBlank() ? "선행 조건을 먼저 완료하세요." : step.prompt());
            return;
        }
        boolean solved = switch (step.kind()) {
            case "present" -> PresentPuzzleDialog.play(this, step.id());
            case "gears" -> RepairPuzzleDialog.play(this, true);
            case "wires" -> RepairPuzzleDialog.play(this, false);
            case "confirm" -> JOptionPane.showConfirmDialog(this, "USB에 연구 데이터를 복사할까요?",
                "데이터 복사", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            case "action" -> true;
            case "rush_hour" -> {
                // This dialog owns the guarded delayed completion of the existing Rush Hour step.
                new ComputerPuzzleDialog(this, state, step.id(), () -> {
                    log("연구 데이터 복구 완료. USB에 복사하세요."); refresh();
                }).setVisible(true);
                yield false;
            }
            default -> throw new IllegalArgumentException("Unregistered puzzle UI: " + step.kind());
        };
        if (solved) {
            state.complete(step.id());
            log(switch (step.id()) {
                case "PZ004" -> "전력이 복구되었습니다. 조명이 안정되면 두 미션을 자유롭게 진행하세요.";
                case "PZ006" -> "연구 계정 접근 권한을 얻었습니다.";
                case "PZ007" -> "USB 확보 미션 완료. USB는 미래까지 보관됩니다.";
                case "PZ002" -> "과거 작업대에서 '대체 부품'을 획득했습니다. 인벤토리에서 부품을 선택하고 손목 장치를 여세요.";
                case "PZ003" -> "중앙 시간 장치에서 '미래 핵심 부품'을 확보했습니다. 인벤토리의 손목 장치에 장착하세요.";
                default -> step.mission().isBlank() ? step.name() + " 완료" : state.missionNext(step.mission());
            });
            if (step.id().equals("PZ002")) GameAudio.get().play("item_pickup");
            if (step.id().equals("PZ003")) GameAudio.get().play("core_pickup");
        }
        refresh();
    }

    private void interactInventory(String id) {
        switch (id) {
            case "SPARE" -> {
                spareSelected = !spareSelected;
                GameAudio.get().play("file_select");
                log(spareSelected ? "대체 부품 선택됨. 손목 시간여행 장치를 클릭하면 수리할 수 있습니다." : "대체 부품 선택을 해제했습니다.");
                refresh();
            }
            case "CORE" -> { GameAudio.get().play("file_select"); log("미래 핵심 부품을 확보했습니다. 손목 장치를 클릭해 장착할 수 있습니다."); }
            case "USB" -> log(state.usbComplete() ? "데이터 USB는 미래에서 사용할 수 있도록 보관합니다." : "컴퓨터에서 복구 데이터를 복사하세요.");
            case "DEVICE" -> {
                if (!state.done("PZ010")) { log("먼저 시간여행 장치를 조립하세요."); return; }
                GameAudio.get().play("ui_open");
                Era beforeEra = state.era();
                boolean repairedBefore = state.isWatchRepaired();
                boolean installedBefore = state.isCoreInstalled();
                boolean escapedBefore = state.isEscaped();
                new DeviceDialog(this, state, spareSelected, () -> {
                    saveProgress();
                    spareSelected = false;
                    if (!escapedBefore && state.isEscaped()) {
                        GameAudio.get().play("ending");
                        log("최종 좌표 입력 성공! 붕괴하는 미래 연구실에서 탈출했습니다.");
                        JOptionPane.showMessageDialog(this, "TIME ESCAPE\n\n장치 복구와 최종 좌표 입력 완료!\n미래 연구실에서 탈출했습니다.\n\n(프로토타입 엔딩)",
                            "탈출 성공", JOptionPane.INFORMATION_MESSAGE);
                    } else if (!installedBefore && state.isCoreInstalled()) {
                        GameAudio.get().play("device_repair");
                        log("미래 핵심 부품을 장착했습니다. 장치를 다시 열어 최종 좌표를 입력하세요.");
                    } else if (beforeEra == Era.PRESENT && state.era() == Era.PAST) {
                        GameAudio.get().play("time_travel");
                        GameAudio.get().era(state.era());
                        log("과거 연구실로 이동했습니다. 작업대 위 황동 장치를 조사해 보세요.");
                    } else if (!repairedBefore && state.isWatchRepaired()) {
                        GameAudio.get().play("device_repair");
                        log("손목 장치를 임시 수리했습니다. 장치를 다시 열어 미래로 이동하세요.");
                    } else if (beforeEra == Era.PAST && state.era() == Era.FUTURE) {
                        GameAudio.get().play("time_travel");
                        GameAudio.get().era(state.era());
                        log("미래 연구실에 도착했습니다. 중앙 시간 실험 장치를 조사하세요.");
                    }
                    refresh();
                }).setVisible(true);
            }
            default -> { }
        }
    }
}


