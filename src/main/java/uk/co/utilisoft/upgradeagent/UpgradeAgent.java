package uk.co.utilisoft.upgradeagent;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/*
 * @author Gareth Morris 21/08/2019
 */
public class UpgradeAgent {

  private final static Logger LOGGER = Logger.getLogger(UpgradeAgent.class.getName());

  private final static String PARENT = new File(System.getProperty("user.dir")).getParent();

  private static final String UPGRADE_JAR_LOCATION = PARENT + "/Upgrade/";

  private static final String JAR_LOCATION = PARENT;

  private static final String BACKUP_JAR_LOCATION = PARENT + "/backup/";

  private static final String PROPS_FILE_LOCATION = PARENT + "/deployment/";

  private static final String UAD_AGENT_JAR = "UadAgent.jar";

  private static String UAD_AGENT_NAME;

  public static void main(String[] args) throws InterruptedException {
    LOGGER.info("Started Agent Upgrade...");
    if (args[0] != null) {
      UAD_AGENT_NAME = args[0];
    }
    else {
      UAD_AGENT_NAME = "uadagent"; // default
    }

    LOGGER.info("UAD Agent Service Name : " + args[0]);
    LOGGER.info(PARENT);
    Thread.sleep(15000);
    File folder = new File(UPGRADE_JAR_LOCATION);
    File[] listOfFiles = folder.listFiles();

    for (File file : listOfFiles) {
      if (file.isFile()) {
        if (file.getName().equals(UAD_AGENT_JAR)) {
          try {
            upgradeJar(file);
          }
          catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
        else if (file.getName().startsWith("application")) {
          upgradePropertiesFile(file);
        }
      }
    }

    startUadAgentService();

    LOGGER.info("Completed upgrade.");
  }

  private static boolean upgradeJar(File newJar) throws IOException {
    LOGGER.info("Entered upgrade function with file name: " + newJar.getAbsolutePath());

    File currentJarLocation = new File(JAR_LOCATION);
    File[] listOfFiles = currentJarLocation.listFiles();

    for (File file : listOfFiles) {
      if (file.getName().contains(UAD_AGENT_JAR)) {

        // backup
        backupCurrentJar(file);

        LOGGER.info("Copying new jar from upgraded folder : " + newJar.getAbsolutePath());
        FileUtils.copyFileToDirectory(newJar, currentJarLocation);
        LOGGER.info("Copied...");
      }
    }
    return true;
  }

  private static boolean upgradePropertiesFile(File newPropsFile) {
    System.out.println("props replace " + newPropsFile.getName() + " " + PROPS_FILE_LOCATION);
    return true;
  }

  private static void backupCurrentJar(File backupFile) throws IOException {
    LOGGER.info("Entered backup function : " + backupFile.getAbsolutePath());
    File backupFolder = new File(BACKUP_JAR_LOCATION);

    if (!backupFolder.exists()) { // create backup
      FileUtils.forceMkdir(backupFolder);
    }

    // delete contents of backup folder
    for (File each : backupFolder.listFiles()) {
      if (each.getName().equals(backupFile.getName())) {
        LOGGER.info("Deleting file : " + each.getAbsolutePath());
        FileUtils.forceDelete(each);
        LOGGER.info("Deleted...");
      }
    }

    // backup
    LOGGER.info("Backing-up old jar file: " + backupFile.getAbsolutePath());
    FileUtils.copyFileToDirectory(backupFile, backupFolder);
    LOGGER.info("Backed-up...");

    LOGGER.info("Deleting current jar file : " + backupFile.getAbsolutePath());
    FileUtils.forceDelete(backupFile);
    LOGGER.info("Deleted...");
  }

  private static void startUadAgentService()  {
    LOGGER.info("Starting UAD Agent Service...");

    String command = System.getProperty("user.dir") + "\\nssm.exe start " + UAD_AGENT_NAME;
    LOGGER.info("CMD: " + command);
    try {
      Runtime.getRuntime().exec(command);
    }
    catch (IOException ioe) {
      LOGGER.error(ioe.getMessage());
    }
  }
}
