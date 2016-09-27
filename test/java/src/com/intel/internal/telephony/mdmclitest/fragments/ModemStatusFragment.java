/*
 * Copyright (C) 2015 Intel Corporation, All rights Reserved.
 *
 * Modem Client library has been designed by:
 *  - Cesar De Oliveira <cesar.de.oliveira@intel.com>
 *  - Lionel Ulmer <lionel.ulmer@intel.com>
 *  - Marc Bellanger <marc.bellanger@intel.com>
 *
 * Original contributor for the test app is:
 *  - Edward Marmounier <edward.marmounier@intel.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.internal.telephony.mdmclitest.fragments;

import com.intel.internal.telephony.AsyncOperationResultListener;
import com.intel.internal.telephony.ModemEventListener;
import com.intel.internal.telephony.ModemStatus;
import com.intel.internal.telephony.ModemStatusManager;
import com.intel.internal.telephony.mdmclitest.R;
import com.intel.internal.telephony.mdmclitest.helpers.MessageBoxHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ModemStatusFragment extends Fragment implements
ModemEventListener, OnClickListener {
    private TextView textViewClientStatus = null;
    private TextView textViewModemStatus = null;
    private Button buttonRequestUpdate = null;
    private Button buttonRequestReset = null;
    private Button buttonRequestShutdown = null;
    private Button buttonRequestLock = null;
    private Button buttonRequestRelease = null;

    private ModemStatusManager modemManager = null;
    public static final String INSTANCE_ID = "InstanceId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int instanceId = ModemStatusManager.getDefaultInstanceId();
        Bundle args = getArguments();
        if (null != args) {
            instanceId = args.getInt(INSTANCE_ID);
        }

        try {
            this.modemManager = ModemStatusManager.getInstance(getActivity(), instanceId);
            this.modemManager.subscribeToEvent(this, ModemStatus.ALL);
        } catch (Exception ex) {
            MessageBoxHelper.showException(this.getActivity(), ex);
        }
    }

    @Override
    public void onStop() {
        super.onDestroy();

        try {
            if (this.modemManager != null) {
                this.modemManager.disconnect();
                this.modemManager = null;
            }
        } catch (Exception ex) {
            MessageBoxHelper.showException(this.getActivity(), ex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.modem_status, container, false);

        if (ret != null) {
            this.textViewClientStatus = (TextView)ret
                                        .findViewById(R.id.textViewClientStatusValue);
            this.textViewModemStatus = (TextView)ret
                                       .findViewById(R.id.textViewModemStatusValue);

            this.buttonRequestUpdate = (Button)ret
                                       .findViewById(R.id.buttonRequestUpdate);
            this.buttonRequestReset = (Button)ret
                                      .findViewById(R.id.buttonRequestReset);
            this.buttonRequestShutdown = (Button)ret
                                         .findViewById(R.id.buttonRequestShutdown);
            this.buttonRequestLock = (Button)ret
                                     .findViewById(R.id.buttonRequestLock);
            this.buttonRequestRelease = (Button)ret
                                        .findViewById(R.id.buttonRequestRelease);

            if (this.buttonRequestUpdate != null) {
                this.buttonRequestUpdate.setOnClickListener(this);
            }

            if (this.buttonRequestReset != null) {
                this.buttonRequestReset.setOnClickListener(this);
            }

            if (this.buttonRequestShutdown != null) {
                this.buttonRequestShutdown.setOnClickListener(this);
            }

            if (this.buttonRequestLock != null) {
                this.buttonRequestLock.setOnClickListener(this);
            }

            if (this.buttonRequestRelease != null) {
                this.buttonRequestRelease.setOnClickListener(this);
            }
        }

        if (this.textViewClientStatus != null) {
            this.textViewClientStatus.setText("CONNECTING...");
        }

        if (this.modemManager != null) {
            this.modemManager.connectAsync(getResources().getString(R.string.app_name),
                                           new AsyncOperationResultListener() {
                                               @Override
                                               public void onOperationError(Exception ex) {
                                                   ModemStatusFragment.this.setUIDisconnectedMode();
                                               }

                                               @Override
                                               public void onOperationComplete() {
                                                   ModemStatusFragment.this.setUIConnectedMode();
                                               }
                                           });
        } else {
            ModemStatusFragment.this.setUIDisconnectedMode();
        }
        return ret;
    }

    @Override
    public void onModemDead() {
        if (this.textViewModemStatus != null) {
            this.textViewModemStatus.setText("MODEM DEAD");
        }
    }

    @Override
    public void onModemDown() {
        if (this.textViewModemStatus != null) {
            this.textViewModemStatus.setText("MODEM DOWN");
        }
    }

    @Override
    public void onModemUp() {
        if (this.textViewModemStatus != null) {
            this.textViewModemStatus.setText("MODEM UP");
        }
    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            try {
                switch (view.getId()) {
                case R.id.buttonRequestUpdate:
                    this.doUpdateModem();
                    break;
                case R.id.buttonRequestReset:
                    this.doResetModem();
                    break;
                case R.id.buttonRequestShutdown:
                    this.doShutdownModem();
                    break;
                case R.id.buttonRequestLock:
                    this.doLockModem();
                    break;
                case R.id.buttonRequestRelease:
                    this.doReleaseModem();
                    break;
                }
            } catch (Exception ex) {
                MessageBoxHelper.showException(this.getActivity(), ex);
            }
        }
    }

    private void doUpdateModem() {
        if (this.modemManager != null) {
            this.modemManager.updateModemAsync(new AsyncOperationResultListener() {
                                                   @Override
                                                   public void onOperationError(Exception ex) {
                                                       MessageBoxHelper.showException(
                                                           ModemStatusFragment.this.getActivity(), ex);
                                                   }

                                                   @Override
                                                   public void onOperationComplete() {
                                                       Toast.makeText(ModemStatusFragment.this.getActivity(),
                                                                      "Update request sent",
                                                                      Toast.LENGTH_SHORT).show();
                                                   }
                                               });
        }
    }

    private void doResetModem() {
        if (this.modemManager != null) {
            this.modemManager.resetModemAsync(new AsyncOperationResultListener() {
                                                  @Override
                                                  public void onOperationError(Exception ex) {
                                                      MessageBoxHelper.showException(
                                                          ModemStatusFragment.this.getActivity(), ex);
                                                  }

                                                  @Override
                                                  public void onOperationComplete() {
                                                      Toast.makeText(ModemStatusFragment.this.getActivity(),
                                                                     "Reset request sent",
                                                                     Toast.LENGTH_SHORT).show();
                                                  }
                                              });
        }
    }

    private void doShutdownModem() {
        if (this.modemManager != null) {
            this.modemManager.shutdownModemAsync(new AsyncOperationResultListener() {
                                                     @Override
                                                     public void onOperationError(Exception ex) {
                                                         MessageBoxHelper.showException(
                                                             ModemStatusFragment.this.getActivity(), ex);
                                                     }

                                                     @Override
                                                     public void onOperationComplete() {
                                                         Toast.makeText(ModemStatusFragment.this.getActivity(),
                                                                        "Shutdown request sent",
                                                                        Toast.LENGTH_SHORT).show();
                                                     }
                                                 });
        }
    }

    private void doLockModem() {
        if (this.modemManager != null) {
            this.modemManager.acquireModemAsync(new AsyncOperationResultListener() {
                                                    @Override
                                                    public void onOperationError(Exception ex) {
                                                        MessageBoxHelper.showException(
                                                            ModemStatusFragment.this.getActivity(), ex);
                                                    }

                                                    @Override
                                                    public void onOperationComplete() {
                                                        Toast.makeText(ModemStatusFragment.this.getActivity(),
                                                                       "Acquire request sent",
                                                                       Toast.LENGTH_SHORT).show();
                                                    }
                                                });
        }
    }

    private void doReleaseModem() {
        if (this.modemManager != null) {
            this.modemManager.releaseModemAsync(new AsyncOperationResultListener() {
                                                    @Override
                                                    public void onOperationError(Exception ex) {
                                                        MessageBoxHelper.showException(
                                                            ModemStatusFragment.this.getActivity(), ex);
                                                    }

                                                    @Override
                                                    public void onOperationComplete() {
                                                        Toast.makeText(ModemStatusFragment.this.getActivity(),
                                                                       "Release request sent",
                                                                       Toast.LENGTH_SHORT).show();
                                                    }
                                                });
        }
    }

    private void setUIConnectedMode() {
        if (this.textViewClientStatus != null) {
            this.textViewClientStatus.setText("CONNECTED");
        }
        if (this.buttonRequestLock != null) {
            this.buttonRequestLock.setEnabled(true);
        }
        if (this.buttonRequestUpdate != null) {
            this.buttonRequestUpdate.setEnabled(true);
        }
        if (this.buttonRequestRelease != null) {
            this.buttonRequestRelease.setEnabled(true);
        }
        if (this.buttonRequestReset != null) {
            this.buttonRequestReset.setEnabled(true);
        }
        if (this.buttonRequestShutdown != null) {
            this.buttonRequestShutdown.setEnabled(true);
        }
    }

    private void setUIDisconnectedMode() {
        if (this.textViewClientStatus != null) {
            this.textViewClientStatus.setText("DISCONNECTED");
        }
        if (this.buttonRequestLock != null) {
            this.buttonRequestLock.setEnabled(false);
        }
        if (this.buttonRequestUpdate != null) {
            this.buttonRequestUpdate.setEnabled(false);
        }
        if (this.buttonRequestRelease != null) {
            this.buttonRequestRelease.setEnabled(false);
        }
        if (this.buttonRequestReset != null) {
            this.buttonRequestReset.setEnabled(false);
        }
        if (this.buttonRequestShutdown != null) {
            this.buttonRequestShutdown.setEnabled(false);
        }
    }
}
