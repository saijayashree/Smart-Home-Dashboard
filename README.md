# Smart-Home-Dashboard
A Java Swing project that simulates a smart home dashboard.

## Overview
 It allows control of devices (Light, Fan, AC, TV), shows energy usage, weather info, and alerts. Built to practice GUI design and event handling.

## Features
- Device cards with ON/OFF toggle
- Turn all devices ON/OFF buttons
- Energy usage progress bars
- Alerts & notifications panel
- Real‑time clock display
- Weather info tiles

## Components (Software)
- Java Swing (GUI)
- Event handling with ActionListeners
- Custom panels for devices, energy, alerts

## Code Preview
```java
JButton allOnBtn = makeButton("⚡  Turn All ON", new Color(60, 150, 100));
allOnBtn.addActionListener(e -> {
    lightCard.turnOn(); fanCard.turnOn(); acCard.turnOn(); tvCard.turnOn();
    statusLabel.setText("All devices turned ON");
    addAlert("✅ All devices turned ON at " +
        LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
    refreshEnergy(energyPanel, lightCard, fanCard, acCard, tvCard);
});
