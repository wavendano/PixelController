/**
 * $Id: InitApplication.java,v 1.1 2010/10/01 15:59:01 mvogt Exp $
 *
 * @Copyright: United Security Providers., Switzerland, 2012, All Rights Reserved.
 */
package com.neophob.sematrix.setup;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import processing.core.PApplet;

import com.neophob.sematrix.color.ColorSet;
import com.neophob.sematrix.glue.Collector;
import com.neophob.sematrix.output.AdaVision;
import com.neophob.sematrix.output.ArtnetDevice;
import com.neophob.sematrix.output.MiniDmxDevice;
import com.neophob.sematrix.output.NullDevice;
import com.neophob.sematrix.output.Output;
import com.neophob.sematrix.output.OutputDeviceEnum;
import com.neophob.sematrix.output.PixelInvadersDevice;
import com.neophob.sematrix.output.RainbowduinoV2Device;
import com.neophob.sematrix.output.RainbowduinoV3Device;
import com.neophob.sematrix.output.StealthDevice;
import com.neophob.sematrix.output.Tpm2;
import com.neophob.sematrix.output.Tpm2Net;
import com.neophob.sematrix.output.UdpDevice;
import com.neophob.sematrix.properties.ApplicationConfigurationHelper;

/**
 * @author mvogt
 *
 */
public class InitApplication {

    private static final Logger LOG = Logger.getLogger(InitApplication.class.getName());
    
    private static final String APPLICATION_CONFIG_FILENAME = "data/config.properties";
    private static final String PALETTE_CONFIG_FILENAME = "data/palette.properties";

    
    /**
     * load and parse configuration file
     * 
     * @param papplet
     * @return
     * @throws IllegalArgumentException
     */
    public static ApplicationConfigurationHelper loadConfiguration(PApplet papplet) throws IllegalArgumentException {
        Properties config = new Properties();
        try {
            config.load(papplet.createInput(APPLICATION_CONFIG_FILENAME));
            LOG.log(Level.INFO, "Config loaded, {0} entries", config.size());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load Config", e);
            throw new IllegalArgumentException(e);
        }
        
        try {
            return new ApplicationConfigurationHelper(config);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Configuration Error: ", e);
            throw new IllegalArgumentException(e);
        }
    }
    
    
    /**
     * 
     * @return
     * @throws IllegalArgumentException
     */
    public static List<ColorSet> getColorPalettes(PApplet papplet) throws IllegalArgumentException {
        //load palette
        Properties palette = new Properties();
        try {
            palette.load(papplet.createInput(PALETTE_CONFIG_FILENAME));
            List<ColorSet> colorSets = ColorSet.loadAllEntries(palette);

            LOG.log(Level.INFO, "ColorSets loaded, {0} entries", colorSets.size());
            return colorSets;
        } catch (Exception e) {
            
            LOG.log(Level.SEVERE, "Failed to load Config", e);
            throw new IllegalArgumentException("Configuration error!", e);
        }               
    }
    
    
    /**
     * 
     * @param applicationConfig
     * @throws IllegalArgumentException
     */
    public static Output getOutputDevice(Collector collector, ApplicationConfigurationHelper applicationConfig) throws IllegalArgumentException {
        OutputDeviceEnum outputDeviceEnum = applicationConfig.getOutputDevice();
        Output output = null;
        try {
            switch (outputDeviceEnum) {
            case PIXELINVADERS:
                output = new PixelInvadersDevice(applicationConfig, collector.getPixelControllerOutput());
                break;
            case STEALTH:
                output = new StealthDevice(applicationConfig, collector.getPixelControllerOutput());
                break;
            case RAINBOWDUINO_V2:
                output = new RainbowduinoV2Device(applicationConfig, collector.getPixelControllerOutput());
                break;
            case RAINBOWDUINO_V3:
                output = new RainbowduinoV3Device(applicationConfig, collector.getPixelControllerOutput());
                break;
            case ARTNET:
                output = new ArtnetDevice(applicationConfig, collector.getPixelControllerOutput());
                break;
            case MINIDMX:
                output = new MiniDmxDevice(applicationConfig, collector.getPixelControllerOutput());
                break;
            case NULL:
                output = new NullDevice(applicationConfig, collector.getPixelControllerOutput());
                break;
            case ADAVISION:
                output = new AdaVision(applicationConfig, collector.getPixelControllerOutput());
                break;
            case UDP:
                output = new UdpDevice(applicationConfig, collector.getPixelControllerOutput());
                break;
            case TPM2:
                output = new Tpm2(applicationConfig, collector.getPixelControllerOutput());
                break;
            case TPM2NET:
                output = new Tpm2Net(applicationConfig, collector.getPixelControllerOutput());                
                break;
            default:
                throw new IllegalArgumentException("Unable to initialize unknown output device: " + outputDeviceEnum);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"\n\nERROR: Unable to initialize output device: " + outputDeviceEnum, e);
        }
        
        return output;
    }
    
    
    
}
