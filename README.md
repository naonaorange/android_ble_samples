# android_ble_samples
AndroidでBluetooth Low Energyを利用するサンプル集です

| サンプル | 説明 |
-----------|-------------|
| [00_android_ble_enable_chacker][0] | BLEが有効かを確認するのみ
| [01_ble_scan_monitor][1] | Advertiseをスキャンする
| [02_android_ble_connector][2] | PeripheralとBLE接続する
| [03_android_ble_gatt_read_write][3] | BLE接続をして、GATTのcharacteristicのRead/Writeを行う。このサンプルではGAPのDEVICE NAMEの読み込み、書き込み、APPEARRANCEの読み込みを行う
| [04_android_ble_gatt_notification][4] | BLE接続をしてPeripheralから送信されるNotificationを受信する。このサンプルではHEART RATEサービスのHEART RATE MESUREMENTを受信する

[0]: 00_uwp_ble_passive_scan/
[1]: 01_ble_scan_monitor/
[2]: 02_android_ble_connector/
[3]: 03_android_ble_gatt_read_write/
[4]: 04_android_ble_gatt_notification/

## 環境
Windows10 64bit

Android Studio 2.3.3
