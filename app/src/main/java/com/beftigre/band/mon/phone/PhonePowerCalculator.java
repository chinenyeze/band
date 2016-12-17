/*
Copyright (C) 2011 The University of Michigan

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Please send inquiries to powertutor@umich.edu
*/

package com.beftigre.band.mon.phone;

import com.beftigre.band.mon.components.Audio;
import com.beftigre.band.mon.components.CPU;
import com.beftigre.band.mon.components.GPS;
import com.beftigre.band.mon.components.LCD;
import com.beftigre.band.mon.components.OLED;
import com.beftigre.band.mon.components.Threeg;
import com.beftigre.band.mon.components.Wifi.WifiData;
import com.beftigre.band.mon.components.Sensors.SensorData;

public interface PhonePowerCalculator {
  public double getLcdPower(LCD.LcdData data);

  public double getOledPower(OLED.OledData data);

  public double getCpuPower(CPU.CpuData data);

  public double getAudioPower(Audio.AudioData data);

  public double getGpsPower(GPS.GpsData data);

  public double getWifiPower(WifiData data);

  public double getThreeGPower(Threeg.ThreegData data);

  public double getSensorPower(SensorData data);
}

