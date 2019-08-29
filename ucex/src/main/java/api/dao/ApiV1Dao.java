package api.dao;

import java.util.List;
import java.util.Map;

import api.domain.AddressBook;
import api.domain.ExtensionData;
import api.domain.MadnCommand;
import api.domain.NumberTenant;
import api.domain.Site;
import api.domain.Tenant;
import api.domain.UserInfo;

public interface ApiV1Dao {

	public ExtensionData getExtensionInfo(NumberTenant nt);
	
	public MadnCommand getMadnInfo(MadnCommand mc);

	public Tenant getTenantInfo(Tenant param);

	public void logHistoryTable(Map<String, Object> param);

	public void addUserExtension(Map<String, Object> param);

	public void deleteUserExtension(Map<String, Object> param);

	public void deleteUser(Map<String, Object> bo);

	public void deleteUserOption(Map<String, Object> bo);

	public void modifyUser(UserInfo u);

	public void modifyUserOption(UserInfo u);

	public void createUser(UserInfo u);

	public void createUserOption(UserInfo u);

	public void createTenant(Tenant t);

	public void modifyTenant(Tenant t);

	public void deleteTenant(Map<String, Object> param);

	public String getTenantPrefix(Map<String, Object> param);

	// test
	public void deleteMemberMADN(Map<String, Object> param);

	public void addMemberMADN(Map<String, Object> param);

	public void deleteMADN(Map<String, Object> param);

	public void createMADN(Map<String, Object> param);

	public void deleteExtension(Map<String, Object> param);

	public void modifyExtension(ExtensionData data);

	public void createExtension(ExtensionData data);

	public void createSite(Map<String, Object> param);

	public Site getSiteInfo(Site param);

	public void modifySite(Map<String, Object> param);

	public void deleteSite(Map<String, Object> param);

	public int getUserInfoCount(UserInfo u);

	public AddressBook getAddressBookInfo(AddressBook d);

	public void deleteAddressBook(AddressBook d);

	public void modifyAddressBook(AddressBook s);

	public void createAddressBook(AddressBook s);

	public void deleteUserRelation(String username);

	public String convertUCEMedia(String cm);

	public void deleteTenantAdmin(Map<String, Object> param);

	public void createTenantAdmin(Tenant t);

	public void createTenantOrg(Tenant t);

	public void deleteTenantOrg(Map<String, Object> param);

	public void modifyTenantUser(Tenant t);

	public List<String> getTenantUsers(Tenant t);

	public void deleteUserRelation(Map<String, Object> bo);

	public void updateUserDelteExtension(Map<String, Object> bo);

	public void procedureRetiredUser(Map<String, Object> bo);

	public void createCsTenant(Tenant t);

	public void deleteCsTenant(Map<String, Object> param);


}
