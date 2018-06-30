package hust.zeng.utils.pojo;

public class CommandResult {

	private int exitStatus;
	private String printInfo;

	public int getExitStatus() {
		return exitStatus;
    }

	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}

	public String getPrintInfo() {
		return printInfo;
	}

	public void setPrintInfo(String printInfo) {
		this.printInfo = printInfo;
	}

	@Override
	public String toString() {
		return "ExeCommandResult \n------------------\n[exitStatus]\n" + exitStatus + "\n[printInfo]\n" + printInfo
				+ "------------------";
	}

}
