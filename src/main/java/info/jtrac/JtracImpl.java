/*
 * Copyright 2002-2005 the original author or authors.
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

package info.jtrac;

import info.jtrac.domain.BatchInfo;
import info.jtrac.domain.Config;
import info.jtrac.domain.Counts;
import info.jtrac.domain.CountsHolder;
import info.jtrac.domain.Field;
import info.jtrac.domain.History;
import info.jtrac.domain.Item;
import info.jtrac.domain.ItemItem;
import info.jtrac.domain.ItemRefId;
import info.jtrac.domain.Metadata;
import info.jtrac.domain.Role;
import info.jtrac.domain.Space;
import info.jtrac.domain.SpaceSequence;
import info.jtrac.domain.State;
import info.jtrac.domain.StoredSearch;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.repository.AttachmentRepository;
import info.jtrac.repository.ConfigRepository;
import info.jtrac.repository.HistoryRepository;
import info.jtrac.repository.ItemItemRepository;
import info.jtrac.repository.ItemRepository;
import info.jtrac.repository.ItemUserRepository;
import info.jtrac.repository.MetadataRepository;
import info.jtrac.repository.SpaceRepository;
import info.jtrac.repository.SpaceSequenceRepository;
import info.jtrac.repository.StoredSearchRepository;
import info.jtrac.repository.UserRepository;
import info.jtrac.repository.UserSpaceRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class JtracImpl implements Jtrac {

    private static final Logger logger = LoggerFactory.getLogger(JtracImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private ItemItemRepository itemItemRepository;
    @Autowired
    private ItemUserRepository itemUserRepository;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private StoredSearchRepository storedSearchRepository;
    @Autowired
    private UserSpaceRoleRepository userSpaceRoleRepository;
    @Autowired
    private SpaceSequenceRepository spaceSequenceRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MessageSource messageSource;

    private Map<String, String> locales;
    private String defaultLocale = "en";
    private String releaseVersion;
    private String releaseTimestamp;
    private String jtracHome;
    private int attachmentMaxSizeInMb = 5;
    private int sessionTimeoutInMinutes = 30;


    public void setLocaleList(String[] array) {
        locales = new LinkedHashMap<String, String>();
        for (String localeString : array) {
            Locale locale = StringUtils.parseLocaleString(localeString);
            locales.put(localeString, localeString + " - " + locale.getDisplayName());
        }
        logger.info("available locales configured " + locales);
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setReleaseTimestamp(String releaseTimestamp) {
        this.releaseTimestamp = releaseTimestamp;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setJtracHome(String jtracHome) {
        this.jtracHome = jtracHome;
    }

    public String getJtracHome() {
        return jtracHome;
    }

    public int getAttachmentMaxSizeInMb() {
        return attachmentMaxSizeInMb;
    }

    public int getSessionTimeoutInMinutes() {
        return sessionTimeoutInMinutes;
    }

    /**
     * this has not been factored into the util package or a helper class
     * because it depends on the PasswordEncoder configured
     */
    public String generatePassword() {
        byte[] ab = new byte[1];
        Random r = new Random();
        r.nextBytes(ab);
        return passwordEncoder.encode(new String(ab));
    }

    /**
     * this has not been factored into the util package or a helper class
     * because it depends on the PasswordEncoder configured
     */
    public String encodeClearText(String clearText) {
        return passwordEncoder.encode(clearText);
    }

    public Map<String, String> getLocales() {
        return locales;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    private void initDefaultLocale(String localeString) {
        if (localeString == null || !locales.containsKey(localeString)) {
            logger.warn("invalid default locale configured = '" + localeString + "', using " + this.defaultLocale);
        } else {
            this.defaultLocale = localeString;
        }
        logger.info("default locale set to '" + this.defaultLocale + "'");
    }

    private void initAttachmentMaxSize(String s) {
        try {
            this.attachmentMaxSizeInMb = Integer.parseInt(s);
        } catch (Exception e) {
            logger.warn("invalid attachment max size '" + s + "', using " + attachmentMaxSizeInMb);
        }
        logger.info("attachment max size set to " + this.attachmentMaxSizeInMb + " MB");
    }

    private void initSessionTimeout(String s) {
        try {
            this.sessionTimeoutInMinutes = Integer.parseInt(s);
        } catch (Exception e) {
            logger.warn("invalid session timeout '" + s + "', using " + this.sessionTimeoutInMinutes);
        }
        logger.info("session timeout set to " + this.sessionTimeoutInMinutes + " minutes");
    }

    //==========================================================================

    public synchronized void storeItems(List<Item> items) {
        for (Item item : items) {
            item.setSendNotifications(false);
            if (item.getStatus() == State.CLOSED) {
                // we support CLOSED items for import also but for consistency
                // simulate the item first created OPEN and then being CLOSED
                item.setStatus(State.OPEN);
                History history = new History();
                history.setTimeStamp(item.getTimeStamp());
                // need to do this as storeHistoryForItem does some role checks
                // and so to avoid lazy initialization exception
                history.setLoggedBy(loadUser(item.getLoggedBy().getId()));
                history.setAssignedTo(item.getAssignedTo());
                history.setComment("-");
                history.setStatus(State.CLOSED);
                history.setSendNotifications(false);
                // storeItem(item, null);
                // storeHistoryForItem(item.getId(), history, null);
            } else {
                // storeItem(item, null);
            }
        }
    }

    public Item loadItem(long id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Item loadItemByRefId(String refId) {
        ItemRefId itemRefId = new ItemRefId(refId); // throws runtime exception if invalid id
        List<Item> items = itemRepository.findBySpacePrefixCodeAndSequenceNum(itemRefId.getPrefixCode(), itemRefId.getSequenceNum());
        if (items.size() == 0) {
            return null;
        }
        return items.get(0);
    }

    public History loadHistory(long id) {
        return historyRepository.findById(id).orElse(null);
    }

    public int loadCountOfAllItems() {
        return (int) itemRepository.count();
    }

    public List<Item> findAllItems(int firstResult, int batchSize) {
        return itemRepository.findAll().subList(firstResult, firstResult + batchSize);
    }

    public void removeItem(Item item) {
        if (item.getRelatingItems() != null) {
            for (ItemItem itemItem : item.getRelatingItems()) {
                removeItemItem(itemItem);
            }
        }
        if (item.getRelatedItems() != null) {
            for (ItemItem itemItem : item.getRelatedItems()) {
                removeItemItem(itemItem);
            }
        }
        itemRepository.delete(item);
    }

    public void removeItemItem(ItemItem itemItem) {
        itemItemRepository.delete(itemItem);
    }

    public int loadCountOfRecordsHavingFieldNotNull(Space space, Field field) {
        return 0;
    }

    public int bulkUpdateFieldToNull(Space space, Field field) {
        return 0;
    }

    public int loadCountOfRecordsHavingFieldWithValue(Space space, Field field, int optionKey) {
        return 0;
    }

    public int bulkUpdateFieldToNullForValue(Space space, Field field, int optionKey) {
        return 0;
    }

    public int loadCountOfRecordsHavingStatus(Space space, int status) {
        return 0;
    }

    public int bulkUpdateStatusToOpen(Space space, int status) {
        return 0;
    }

    public int bulkUpdateRenameSpaceRole(Space space, String oldRoleKey, String newRoleKey) {
        return 0;
    }

    public int bulkUpdateDeleteSpaceRole(Space space, String roleKey) {
        return 0;
    }

    // =========  Acegi UserDetailsService implementation ==========
    public UserDetails loadUserByUsername(String loginName) {
        List<User> users = null;
        if (loginName.indexOf("@") != -1) {
            users = userRepository.findByEmail(loginName);
        } else {
            users = userRepository.findByLoginName(loginName);
        }
        if (users.size() == 0) {
            throw new UsernameNotFoundException("User not found for '" + loginName + "'");
        }
        logger.debug("loadUserByUserName success for '" + loginName + "'");
        User user = users.get(0);
        // if some spaces have guest access enabled, allocate these spaces as well
        Set<Space> userSpaces = user.getSpaces();
        logger.debug("user spaces: " + userSpaces);
        for (Space s : findSpacesWhereGuestAllowed()) {
            if (!userSpaces.contains(s)) {
                user.addSpaceWithRole(s, Role.ROLE_GUEST);

            }
        }
        for (UserSpaceRole usr : user.getSpaceRoles()) {
            logger.debug("UserSpaceRole: " + usr);

        }
        return user;
    }

    public User loadUser(long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User loadUser(String loginName) {
        List<User> users = userRepository.findByLoginName(loginName);
        if (users.size() == 0) {
            return null;
        }
        return users.get(0);
    }

    public void storeUser(User user) {
        user.clearNonPersistentRoles();
        userRepository.save(user);
    }

    public void storeUser(User user, String password, boolean sendNotifications) {
        if (password == null) {
            password = generatePassword();
        }
        user.setPassword(encodeClearText(password));
        storeUser(user);
        if (sendNotifications) {
            // mailSender.sendUserPassword(user, password);
        }
    }

    public void removeUser(User user) {
        itemUserRepository.deleteAll(itemUserRepository.findByUser(user));
        userRepository.delete(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findUsersWhereIdIn(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    public List<User> findUsersMatching(String searchText, String searchOn) {
        return null;
    }

    public List<User> findUsersForSpace(long spaceId) {
        return userSpaceRoleRepository.findBySpaceId(spaceId).stream().map(UserSpaceRole::getUser).collect(Collectors.toList());
    }

    public List<UserSpaceRole> findUserRolesForSpace(long spaceId) {
        return userSpaceRoleRepository.findBySpaceId(spaceId);
    }

    public Map<Long, List<UserSpaceRole>> loadUserRolesMapForSpace(long spaceId) {
        List<UserSpaceRole> list = userSpaceRoleRepository.findBySpaceId(spaceId);
        Map<Long, List<UserSpaceRole>> map = new LinkedHashMap<Long, List<UserSpaceRole>>();
        for (UserSpaceRole usr : list) {
            long userId = usr.getUser().getId();
            List<UserSpaceRole> value = map.get(userId);
            if (value == null) {
                value = new ArrayList<UserSpaceRole>();
                map.put(userId, value);
            }
            value.add(usr);
        }
        return map;
    }

    public Map<Long, List<UserSpaceRole>> loadSpaceRolesMapForUser(long userId) {
        List<UserSpaceRole> list = userSpaceRoleRepository.findByUserId(userId);
        Map<Long, List<UserSpaceRole>> map = new LinkedHashMap<Long, List<UserSpaceRole>>();
        for (UserSpaceRole usr : list) {
            long spaceId = usr.getSpace() == null ? 0 : usr.getSpace().getId();
            List<UserSpaceRole> value = map.get(spaceId);
            if (value == null) {
                value = new ArrayList<UserSpaceRole>();
                map.put(spaceId, value);
            }
            value.add(usr);
        }
        return map;
    }

    public List<User> findUsersWithRoleForSpace(long spaceId, String roleKey) {
        return null;
    }

    public List<User> findUsersForUser(User user) {
        Set<Space> spaces = user.getSpaces();
        if (spaces.size() == 0) {
            // this will happen when a user has no spaces allocated
            return Collections.emptyList();
        }
        // must be a better way to make this unique?
        List<User> users = new ArrayList<>();
        for (Space space : spaces) {
            users.addAll(findUsersForSpace(space.getId()));
        }
        Set<User> userSet = new LinkedHashSet<User>(users);
        return new ArrayList<User>(userSet);
    }

    public List<User> findUsersNotFullyAllocatedToSpace(long spaceId) {
        return null;
    }

    public int loadCountOfHistoryInvolvingUser(User user) {
        return 0;
    }

    //==========================================================================

    public CountsHolder loadCountsForUser(User user) {
        return null;
    }

    public Counts loadCountsForUserSpace(User user, Space space) {
        return null;
    }

    //==========================================================================

    public void storeUserSpaceRole(User user, Space space, String roleKey) {
        user.addSpaceWithRole(space, roleKey);
        storeUser(user);
    }

    public void removeUserSpaceRole(UserSpaceRole userSpaceRole) {
        User user = userSpaceRole.getUser();
        user.removeSpaceWithRole(userSpaceRole.getSpace(), userSpaceRole.getRoleKey());
        userSpaceRoleRepository.delete(userSpaceRole);
    }

    public UserSpaceRole loadUserSpaceRole(long id) {
        return userSpaceRoleRepository.findById(id).orElse(null);
    }

    //==========================================================================

    public Space loadSpace(long id) {
        return spaceRepository.findById(id).orElse(null);
    }

    public Space loadSpace(String prefixCode) {
        return spaceRepository.findByPrefixCode(prefixCode).orElse(null);
    }

    public void storeSpace(Space space) {
        boolean newSpace = space.getId() == 0;
        spaceRepository.save(space);
        if (newSpace) {
            SpaceSequence ss = new SpaceSequence();
            ss.setNextSeqNum(1);
            ss.setId(space.getId());
            spaceSequenceRepository.save(ss);
        }
    }

    public List<Space> findAllSpaces() {
        return spaceRepository.findAll();
    }

    public List<Space> findSpacesWhereIdIn(List<Long> ids) {
        return spaceRepository.findAllById(ids);
    }

    public List<Space> findSpacesWhereGuestAllowed() {
        return spaceRepository.findByGuestAllowed(true);
    }

    public List<Space> findSpacesNotFullyAllocatedToUser(long userId) {
        return null;
    }

    public void removeSpace(Space space) {
        logger.info("proceeding to delete space: " + space);
        userSpaceRoleRepository.deleteAll(userSpaceRoleRepository.findBySpaceId(space.getId()));
        itemRepository.deleteAll(itemRepository.findBySpace(space));
        spaceRepository.delete(space);
        logger.info("successfully deleted space");
    }

    //==========================================================================

    public void storeMetadata(Metadata metadata) {
        metadataRepository.save(metadata);
    }

    public Metadata loadMetadata(long id) {
        return metadataRepository.findById(id).orElse(null);
    }

    //==========================================================================

    public Map<String, String> loadAllConfig() {
        List<Config> list = configRepository.findAll();
        Map<String, String> allConfig = new HashMap<String, String>(list.size());
        for (Config c : list) {
            allConfig.put(c.getParam(), c.getValue());
        }
        return allConfig;
    }

    // TODO must be some nice generic way to do this
    public void storeConfig(Config config) {
        configRepository.save(config);
        if (config.isLocaleConfig()) {
            initDefaultLocale(config.getValue());
        } else if (config.isAttachmentConfig()) {
            initAttachmentMaxSize(config.getValue());
        } else if (config.isSessionTimeoutConfig()) {
            initSessionTimeout(config.getValue());
        }
    }

    public String loadConfig(String param) {
        Config config = configRepository.findById(param).orElse(null);
        if (config == null) {
            return null;
        }
        String value = config.getValue();
        if (value == null || value.trim().equals("")) {
            return null;
        }
        return value;
    }

    //========================================================

    public void rebuildIndexes(BatchInfo batchInfo) {

    }

    public boolean validateTextSearchQuery(String text) {
        return true;
    }

    //==========================================================================

    public void executeHourlyTask() {
        logger.debug("hourly task called");
    }

    /* configured to be called every five minutes */
    public void executePollingTask() {
        logger.debug("polling task called");
    }

    //==========================================================================

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public String getReleaseTimestamp() {
        return releaseTimestamp;
    }

    @Override
    public List<StoredSearch> loadAllStoredSearch() {

        return storedSearchRepository.findAll();
    }

    //==========================================================================

    @Override
    public void storeStoredSearch(StoredSearch storedSearch) {
        storedSearchRepository.save(storedSearch);
    }

    @Override
    public void removeStoredSearch(Long id) {
        storedSearchRepository.deleteById(id);
    }

    @Override
    public void updateItem(Item item, User user) {
        History history = new History();
        history.setLoggedBy(user);
        history.setAssignedTo(item.getAssignedTo());
        history.setComment(item.getEditReason());
        history.setTimeStamp(new Date());
        history.setStatus(item.getStatus());
        item.add(history);
        itemRepository.save(item);
    }
}
