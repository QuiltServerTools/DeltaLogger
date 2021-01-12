package com.github.fabricservertools.deltalogger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to hold logger with name of application prepended (class name)
 */
public class DeltaLogger {
  public static final Logger LOG = LogManager.getLogger();
}
