import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SmartHomeDashboard {

    static final Color BG_DARK        = new Color(18, 18, 30);
    static final Color CARD_BG        = new Color(30, 30, 50);
    static final Color ACCENT_ON      = new Color(72, 199, 142);
    static final Color ACCENT_OFF     = new Color(100, 100, 130);
    static final Color TEXT_PRIMARY   = new Color(230, 230, 255);
    static final Color TEXT_SECONDARY = new Color(150, 150, 190);
    static final Color HEADER_BG      = new Color(25, 25, 45);

    static DefaultListModel<String> alertModel = new DefaultListModel<>();
    static int alertCount = 0;
    static JLabel alertBadge;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartHomeDashboard::buildUI);
    }

    static void buildUI() {
        JFrame frame = new JFrame("🏠 Smart Home Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(580, 720);
        frame.setResizable(false);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(new BorderLayout());

        // ── HEADER ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Smart Home");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Control your devices");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        JLabel clock = new JLabel();
        clock.setFont(new Font("Segoe UI", Font.BOLD, 15));
        clock.setForeground(ACCENT_ON);
        Timer clockTimer = new Timer(1000, e ->
            clock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"))));
        clockTimer.start();
        clock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(clock, BorderLayout.EAST);

        // ── STATUS BAR ──────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusBar.setBackground(new Color(40, 40, 65));
        statusBar.setBorder(new EmptyBorder(6, 10, 6, 10));
        JLabel statusLabel = new JLabel("All devices are OFF");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusBar.add(statusLabel);

        // ── DEVICE CARDS ─────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setBackground(BG_DARK);
        grid.setBorder(new EmptyBorder(12, 16, 8, 16));

        DeviceCard lightCard = new DeviceCard("💡", "Light",      "Living Room", statusLabel, 40);
        DeviceCard fanCard   = new DeviceCard("🌀", "Fan",        "Bedroom",     statusLabel, 60);
        DeviceCard acCard    = new DeviceCard("❄️", "AC",         "Hall",        statusLabel, 120);
        DeviceCard tvCard    = new DeviceCard("📺", "Television", "Bedroom",     statusLabel, 80);

        grid.add(lightCard);
        grid.add(fanCard);
        grid.add(acCard);
        grid.add(tvCard);

        // ── WEATHER PANEL ────────────────────────────────────────
        JPanel weatherPanel = buildWeatherPanel();

        // ── ENERGY PANEL ─────────────────────────────────────────
        JPanel energyPanel = buildEnergyPanel(lightCard, fanCard, acCard, tvCard);

        // ── ALERTS PANEL ─────────────────────────────────────────
        JPanel alertsPanel = buildAlertsPanel();

        // ── BOTTOM BUTTONS ───────────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottomPanel.setBackground(BG_DARK);
        bottomPanel.setBorder(new EmptyBorder(4, 16, 14, 16));

        JButton allOnBtn = makeButton("⚡  Turn All ON", new Color(60, 150, 100));
        allOnBtn.setPreferredSize(new Dimension(175, 38));
        allOnBtn.addActionListener(e -> {
            lightCard.turnOn(); fanCard.turnOn(); acCard.turnOn(); tvCard.turnOn();
            statusLabel.setText("All devices turned ON");
            statusLabel.setForeground(ACCENT_ON);
            addAlert("✅ All devices turned ON at " +
                LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            refreshEnergy(energyPanel, lightCard, fanCard, acCard, tvCard);
        });

        JButton allOffBtn = makeButton("⏻  Turn All OFF", new Color(200, 60, 80));
        allOffBtn.setPreferredSize(new Dimension(175, 38));
        allOffBtn.addActionListener(e -> {
            lightCard.turnOff(); fanCard.turnOff(); acCard.turnOff(); tvCard.turnOff();
            statusLabel.setText("All devices turned OFF");
            statusLabel.setForeground(new Color(200, 100, 100));
            addAlert("⚠️ All devices turned OFF at " +
                LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            refreshEnergy(energyPanel, lightCard, fanCard, acCard, tvCard);
        });

        bottomPanel.add(allOnBtn);
        bottomPanel.add(allOffBtn);

        // Attach energy refresh callback to all cards
        ActionListener refreshCb = e -> refreshEnergy(energyPanel, lightCard, fanCard, acCard, tvCard);
        lightCard.setRefreshCallback(refreshCb);
        fanCard.setRefreshCallback(refreshCb);
        acCard.setRefreshCallback(refreshCb);
        tvCard.setRefreshCallback(refreshCb);

        // ── ASSEMBLE ────────────────────────────────────────────
        JPanel mainArea = new JPanel();
        mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.Y_AXIS));
        mainArea.setBackground(BG_DARK);
        mainArea.add(weatherPanel);
        mainArea.add(grid);
        mainArea.add(energyPanel);
        mainArea.add(alertsPanel);
        mainArea.add(bottomPanel);

        JScrollPane scroll = new JScrollPane(mainArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setBackground(BG_DARK);

        frame.add(header,    BorderLayout.NORTH);
        frame.add(statusBar, BorderLayout.CENTER);
        frame.add(scroll,    BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ── WEATHER PANEL ────────────────────────────────────────────
    static JPanel buildWeatherPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(new Color(25, 25, 45));
        panel.setBorder(new EmptyBorder(10, 16, 10, 16));

        String[][] data = {
            {"🌤️", "Today",    "34°C",    "Partly Cloudy"},
            {"💧", "Humidity", "68%",     "Moderate"},
            {"💨", "Wind",     "12 km/h", "Breezy"},
            {"🌡️", "Indoor",   "26°C",    "Comfortable"}
        };

        for (String[] d : data) {
            JPanel tile = new JPanel();
            tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
            tile.setBackground(CARD_BG);
            tile.setBorder(new EmptyBorder(8, 10, 8, 10));

            JLabel ico  = label(d[0], new Font("Segoe UI Emoji", Font.PLAIN, 20), new Color(100, 200, 255));
            JLabel val  = label(d[2], new Font("Segoe UI", Font.BOLD, 15),        new Color(100, 200, 255));
            JLabel lbl  = label(d[1], new Font("Segoe UI", Font.PLAIN, 11),       TEXT_SECONDARY);
            JLabel desc = label(d[3], new Font("Segoe UI", Font.ITALIC, 10),      ACCENT_ON);

            for (JLabel l : new JLabel[]{ico, val, lbl, desc}) l.setAlignmentX(Component.CENTER_ALIGNMENT);

            tile.add(ico); tile.add(Box.createVerticalStrut(2));
            tile.add(val); tile.add(lbl); tile.add(desc);
            panel.add(tile);
        }
        return panel;
    }

    // ── ENERGY PANEL ─────────────────────────────────────────────
    static JPanel buildEnergyPanel(DeviceCard... cards) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(new EmptyBorder(4, 16, 8, 16));

        JLabel heading = new JLabel("📊 Energy Usage");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(TEXT_PRIMARY);
        heading.setBorder(new EmptyBorder(4, 0, 6, 0));

        JPanel bars = new JPanel(new GridLayout(cards.length, 1, 0, 6));
        bars.setBackground(BG_DARK);
        bars.setName("ENERGY_BARS");
        for (DeviceCard c : cards) bars.add(makeEnergyRow(c));

        wrapper.add(heading, BorderLayout.NORTH);
        wrapper.add(bars,    BorderLayout.CENTER);
        return wrapper;
    }

    static JPanel makeEnergyRow(DeviceCard card) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG_DARK);

        JLabel name = new JLabel(card.deviceName);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        name.setForeground(TEXT_SECONDARY);
        name.setPreferredSize(new Dimension(85, 16));

        int watts = card.isOn ? card.wattage : 0;
        JProgressBar bar = new JProgressBar(0, 150);
        bar.setValue(watts);
        bar.setStringPainted(true);
        bar.setString(watts + " W");
        bar.setBackground(new Color(40, 40, 65));
        bar.setForeground(card.isOn ? ACCENT_ON : ACCENT_OFF);
        bar.setBorderPainted(false);
        bar.setFont(new Font("Segoe UI", Font.BOLD, 11));

        row.add(name, BorderLayout.WEST);
        row.add(bar,  BorderLayout.CENTER);
        return row;
    }

    static void refreshEnergy(JPanel energyPanel, DeviceCard... cards) {
        JPanel bars = (JPanel) energyPanel.getComponent(1);
        bars.removeAll();
        for (DeviceCard c : cards) bars.add(makeEnergyRow(c));
        bars.revalidate();
        bars.repaint();

        int total = 0;
        for (DeviceCard c : cards) if (c.isOn) total += c.wattage;
        if (total > 200)
            addAlert("⚡ High energy usage: " + total + "W — consider turning some devices off.");
    }

    // ── ALERTS PANEL ─────────────────────────────────────────────
    static JPanel buildAlertsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(new EmptyBorder(4, 16, 8, 16));

        JPanel hRow = new JPanel(new BorderLayout());
        hRow.setOpaque(false);

        JLabel heading = new JLabel("🔔 Alerts & Notifications");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(TEXT_PRIMARY);
        heading.setBorder(new EmptyBorder(4, 0, 6, 0));

        alertBadge = new JLabel("0");
        alertBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        alertBadge.setForeground(Color.WHITE);
        alertBadge.setBackground(new Color(200, 60, 80));
        alertBadge.setOpaque(true);
        alertBadge.setBorder(new EmptyBorder(2, 7, 2, 7));

        JButton clearBtn = new JButton("Clear All");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setBackground(new Color(60, 60, 90));
        clearBtn.setForeground(TEXT_SECONDARY);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            alertModel.clear();
            alertCount = 0;
            alertBadge.setText("0");
        });

        JPanel rightH = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightH.setOpaque(false);
        rightH.add(alertBadge);
        rightH.add(clearBtn);

        hRow.add(heading, BorderLayout.WEST);
        hRow.add(rightH,  BorderLayout.EAST);

        JList<String> alertList = new JList<>(alertModel);
        alertList.setBackground(CARD_BG);
        alertList.setForeground(TEXT_PRIMARY);
        alertList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alertList.setFixedCellHeight(26);
        alertList.setBorder(new EmptyBorder(4, 8, 4, 8));

        JScrollPane sp = new JScrollPane(alertList);
        sp.setPreferredSize(new Dimension(0, 95));
        sp.setBorder(new LineBorder(new Color(60, 60, 90), 1));
        sp.getVerticalScrollBar().setUnitIncrement(8);

        addAlert("🏠 Dashboard started — welcome home!");

        wrapper.add(hRow, BorderLayout.NORTH);
        wrapper.add(sp,   BorderLayout.CENTER);
        return wrapper;
    }

    // ── HELPERS ──────────────────────────────────────────────────
    static void addAlert(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        alertModel.add(0, "[" + time + "]  " + msg);
        alertCount++;
        if (alertBadge != null) alertBadge.setText(String.valueOf(alertCount));
    }

    static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

// ── DEVICE CARD ──────────────────────────────────────────────────
class DeviceCard extends JPanel {

    static final Color BG_ON          = new Color(40, 70, 60);
    static final Color BG_OFF         = new Color(30, 30, 50);
    static final Color ACCENT_ON      = new Color(72, 199, 142);
    static final Color ACCENT_OFF     = new Color(100, 100, 130);
    static final Color TEXT_PRIMARY   = new Color(230, 230, 255);
    static final Color TEXT_SECONDARY = new Color(150, 150, 190);

    boolean isOn = false;
    String  deviceName;
    int     wattage;

    JLabel  statusDot, statusText;
    JButton toggleBtn;
    JLabel  globalStatus;
    ActionListener refreshCallback;

    DeviceCard(String icon, String name, String room, JLabel globalStatus, int wattage) {
        this.deviceName   = name;
        this.globalStatus = globalStatus;
        this.wattage      = wattage;

        setLayout(new BorderLayout(0, 6));
        setBackground(BG_OFF);
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(60, 60, 90), 1, true),
            new EmptyBorder(14, 14, 14, 14)
        ));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Top: icon + status dot
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        statusDot = new JLabel("●");
        statusDot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusDot.setForeground(ACCENT_OFF);
        topRow.add(iconLabel, BorderLayout.WEST);
        topRow.add(statusDot, BorderLayout.EAST);

        // Middle: name, room, wattage
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(TEXT_PRIMARY);
        JLabel roomLabel = new JLabel(room);
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roomLabel.setForeground(TEXT_SECONDARY);
        JLabel wattLabel = new JLabel(wattage + "W max");
        wattLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        wattLabel.setForeground(new Color(120, 120, 160));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        namePanel.add(nameLabel);
        namePanel.add(roomLabel);
        namePanel.add(wattLabel);

        // Bottom: status + toggle button
        statusText = new JLabel("OFF");
        statusText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusText.setForeground(ACCENT_OFF);

        toggleBtn = new JButton("Turn ON");
        toggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        toggleBtn.setBackground(ACCENT_ON);
        toggleBtn.setForeground(Color.WHITE);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setPreferredSize(new Dimension(86, 28));
        toggleBtn.addActionListener(e -> toggle());

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.add(statusText, BorderLayout.WEST);
        bottomRow.add(toggleBtn,  BorderLayout.EAST);

        add(topRow,    BorderLayout.NORTH);
        add(namePanel, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);
    }

    void setRefreshCallback(ActionListener cb) { this.refreshCallback = cb; }

    void toggle() {
        isOn = !isOn;
        updateVisuals();
        globalStatus.setText(deviceName + " is " + (isOn ? "ON ✅" : "OFF"));
        globalStatus.setForeground(isOn ? ACCENT_ON : new Color(200, 100, 100));
        SmartHomeDashboard.addAlert(
            (isOn ? "✅ " : "🔴 ") + deviceName + " turned " + (isOn ? "ON" : "OFF") +
            " at " + java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
        if (refreshCallback != null) refreshCallback.actionPerformed(null);
    }

    void turnOn()  { isOn = true;  updateVisuals(); if (refreshCallback != null) refreshCallback.actionPerformed(null); }
    void turnOff() { isOn = false; updateVisuals(); if (refreshCallback != null) refreshCallback.actionPerformed(null); }

    void updateVisuals() {
        setBackground(isOn ? BG_ON : BG_OFF);
        statusDot.setForeground(isOn ? ACCENT_ON : ACCENT_OFF);
        statusText.setText(isOn ? "ON" : "OFF");
        statusText.setForeground(isOn ? ACCENT_ON : ACCENT_OFF);
        toggleBtn.setText(isOn ? "Turn OFF" : "Turn ON");
        toggleBtn.setBackground(isOn ? new Color(200, 80, 80) : ACCENT_ON);
        repaint();
    }
}