package com.mg.btreceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.UUID;

public class BtReceiverAct extends AppCompatActivity {
    private static final String TAG               = BtReceiverAct.class.getSimpleName();
    static final         int    REQUEST_ENABLE_BT = 1;
    private BluetoothGattServer btServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_receiver);

        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (btAdapter == null) {
            Log.e(TAG, "No BT Adapter!");
            return;
        }

        if (!btAdapter.isEnabled()) {
            Log.e(TAG, "BT not enabled");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return;
        }
        btAdapter.setName("mg Test");

        btServer = btManager.openGattServer(this, new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());

                super.onConnectionStateChange(device, status, newState);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                                    int offset,
                                                    BluetoothGattCharacteristic characteristic) {
                Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                final byte byteArr[] = new byte[20];
                final int  status    = BluetoothGatt.GATT_SUCCESS;
                btServer.sendResponse(device, requestId, status, offset, byteArr);
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                     BluetoothGattCharacteristic characteristic,
                                                     boolean preparedWrite, boolean responseNeeded,
                                                     int offset, byte[] value) {
                Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                final byte byteArr[] = new byte[20];
                final int  status    = BluetoothGatt.GATT_SUCCESS;
                btServer.sendResponse(device, requestId, status, offset, byteArr);
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                final byte byteArr[] = new byte[20];
                final int  status    = BluetoothGatt.GATT_SUCCESS;
                btServer.sendResponse(device, requestId, status, 0, byteArr);
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
            }
        });

        Log.i(TAG, btServer.toString());


        final UUID SERVICE_UUID = UUID.randomUUID();
        BluetoothGattService service =
                new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        final UUID CHARACTERISTIC_NUM_UUID = UUID.randomUUID();
        BluetoothGattCharacteristic offsetCharacteristic =
                new BluetoothGattCharacteristic(CHARACTERISTIC_NUM_UUID,
                                                //Read+write permissions
                                                BluetoothGattCharacteristic.PROPERTY_READ |
                                                BluetoothGattCharacteristic.PROPERTY_WRITE,
                                                BluetoothGattCharacteristic.PERMISSION_READ |
                                                BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(offsetCharacteristic);

        btServer.addService(service);

        AdvertiseSettings settings =
                new AdvertiseSettings.Builder().setConnectable(true).setTimeout(0).build();
        AdvertiseData data = new AdvertiseData.Builder().setIncludeDeviceName(true).
                addServiceUuid(new ParcelUuid(service.getUuid())).build();

        AdvertiseCallback adCb = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + settingsInEffect.toString());
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
                super.onStartFailure(errorCode);
            }
        };

        btAdapter.getBluetoothLeAdvertiser().startAdvertising(settings, data, adCb);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.d(TAG, data.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bt_receiver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
