package net.vivekiyer.GAL;

public class CorporateAddressBook {

	private ActiveSyncManager asMgr;

	public CorporateAddressBook() throws Exception{
		asMgr = new ActiveSyncManager();
		asMgr.setServerName("mail.example.com");
		asMgr.setDomain("DOMAIN");
		asMgr.setmUsername("username");
		asMgr.setPassword("password");
		asMgr.setUseSSL(true);
		asMgr.setAcceptAllCerts(true);
		asMgr.Initialize();
		asMgr.getExchangeServerVersion();

		System.out.println("" +
				"Version="+ asMgr.getActiveSyncVersion()
				+ "\npolicyKey="+ asMgr.getPolicyKey()
				+ "\ndeviceId="+ asMgr.getDeviceId()
				);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CorporateAddressBook cab = new CorporateAddressBook();
			cab.asMgr.searchGAL("sample");
			Debug.printLog();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
