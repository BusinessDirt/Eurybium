package github.businessdirt.eurybium.commands

enum class CommandCategory(val color: String, val categoryName: String, val description: String) {
    MAIN(
        "§6",
        "Main Command",
        "Most useful commands of SkyHanni"
    ),
    USERS_ACTIVE(
        "§e",
        "Normal Command",
        "Normal Command for everyone to use",
    ),
    DEVELOPER_TEST(
        "§5",
        "Developer Test Commands",
        "A Command to edit/test/change some features. §cIntended for developers only!"
    ),
    DEVELOPER_DEBUG(
        "§9",
        "Developer Debug Commands",
        "A Command to debug/read/copy/monitor features. §cIntended for developers only!"
    )
    ;

    companion object {
        val developmentCategories = listOf(DEVELOPER_DEBUG, DEVELOPER_TEST)
    }
}
