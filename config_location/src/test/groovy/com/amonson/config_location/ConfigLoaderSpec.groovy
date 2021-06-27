package com.amonson.config_location

import com.amonson.prop_store.PropMap
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisConnectionException
import spock.lang.Specification

import java.util.logging.Logger

// This class does not test the Redis configure location as that should be treated as a functional test not a unit test.
// Effort was made to mock the Jedis code but no real processing occurs. Need functional test for this.
class ConfigLoaderSpec extends Specification {
    Jedis createMockInstance(HostAndPort authority) {
        Jedis instance = Mock(Jedis)
        instance.hexists(_ as String, _ as String) >> true
        instance.hget(_ as String, _ as String) >> "{}"
        return instance
    }

    def underTest_
    def setup() {
        ConfigLoader.ETC_DIR = "./build/tmp"
        underTest_ = new ConfigLoader("testApp", Mock(Logger))
        underTest_.factory_ = this::createMockInstance

        underTest_.userDir_ = "./build/tmp/user/testApp.d"
        underTest_.systemDir_ = "./build/tmp/system/testApp.d"
        underTest_.customDir_ = "./build/tmp/etc"
        File f = new File(underTest_.userDir_)
        f.mkdirs()
        f = new File(underTest_.systemDir_)
        f.mkdirs()
        f = new File(underTest_.customDir_)
        f.mkdirs()

        new File(ConfigLoader.ETC_DIR + "/testApp.json").text = """{
    "custom_dir": "",
    "redis_server": "localhost",
    "redis_port": 6379,
    "redis_db": 1,
    "redis_secret": "secret"
}"""
    }

    def cleanup() {
        File f = new File(underTest_.userDir_)
        f.deleteDir()
        f = new File(underTest_.systemDir_)
        f.deleteDir()
        f = new File(underTest_.customDir_)
        f.deleteDir()
    }

    def "Test ctor"() {
        given:
            def config = new ConfigLoader(NAME, Mock(Logger))
        expect:  config.applicationName_ == RESULT
            and: config.jsonParser_ != null
            and: config.yamlParser_ != null
            and: config.userDir_ == (System.getProperty("user.home") + "/.config/" + NAME + ".d")
            and: config.systemDir_ == ("/etc/" + NAME + ".d")
        where:
            NAME         || RESULT
            "qwerty"     || "qwerty"
            "Qwerty-_2"  || "Qwerty-_2"
            "name----"   || "name----"
            "name_Red"   || "name_Red"
            "name10-Red" || "name10-Red"
    }

    def "Test ctor negative"() {
        when:
            new ConfigLoader(NAME, LOGGER)
        then:
            thrown(IllegalArgumentException)
        where:
            NAME          | LOGGER
            null          | Mock(Logger)
            ""            | Mock(Logger)
            "_qwerty"     | Mock(Logger)
            "-Qwerty-_2"  | Mock(Logger)
            "00name---"   | Mock(Logger)
            "name#Red"    | Mock(Logger)
            "name10 Red"  | Mock(Logger)
            "name10-Red"  | null
    }

    def "Test good JSON in user folder"() {
        given:
            new File((String)underTest_.userDir_ + "/test.json").text = goodJson_
            PropMap props = underTest_.getConfigurationByName("test")
        expect:
            props.getBoolean("property")
        and:
            props.getString("type") == "json"
    }

    def "Test good JSON in system folder"() {
        given:
            new File((String)underTest_.systemDir_ + "/test.json").text = goodJson_
            PropMap props = underTest_.getConfigurationByName("test")
        expect:
            props.getBoolean("property")
        and:
            props.getString("type") == "json"
    }

    def "Test good YAML in user folder"() {
        given:
            new File((String)underTest_.userDir_ + "/test.yml").text = goodYaml_
            PropMap props = underTest_.getConfigurationByName("test")
        expect:
            props.getBoolean("property")
        and:
            props.getString("type") == "yaml"
    }

    def "Test good YAML in system folder"() {
        given:
            new File((String)underTest_.userDir_ + "/test.yml").text = goodYaml_
            PropMap props = underTest_.getConfigurationByName("test")
        expect:
            props.getBoolean("property")
        and:
            props.getString("type") == "yaml"
    }

    def "Test good JSON overriding values"() {
        given:
            new File((String)underTest_.systemDir_ + "/test.json").text = goodJsonSystem_
            new File((String)underTest_.userDir_ + "/test.json").text = goodJsonUser_
            PropMap props = underTest_.getConfigurationByName("test")
        expect:
            props.getBoolean("property")
        and:
            props.getString("type") == "json"
        and:
            props.getMap("overridden").getString("key1") == null
        and:
            props.getMap("overridden").getString("key2") == "overrode"
    }

    def "Test good YAML overriding values"() {
        given:
            new File((String)underTest_.systemDir_ + "/test.yml").text = goodYamlSystem_
            new File((String)underTest_.userDir_ + "/test.yml").text = goodYamlUser_
            PropMap props = underTest_.getConfigurationByName("test")
        expect:
            props.getBoolean("property")
        and:
            props.getString("type") == "yaml"
        and:
            props.getMap("overridden").getString("key2") == null
        and:
            props.getMap("overridden").getString("key1") == "overrode"
    }

    def "Test good JSON overriding with YAML values"() {
        given:
            new File((String)underTest_.systemDir_ + "/test.json").text = goodJsonSystem_
            new File((String)underTest_.userDir_ + "/test.yml").text = goodYamlUser_
            PropMap props = underTest_.getConfigurationByName("test")
        expect:
            props.getBoolean("property")
        and:
            props.getString("type") == "yaml"
        and:
            props.getMap("overridden").getString("key2") == null
        and:
            props.getMap("overridden").getString("key1") == "overrode"
    }

    def "Test bad JSON"() {
        given:
            new File((String)underTest_.systemDir_ + "/test.json").text = badJson_
            PropMap props = underTest_.getConfigurationByName("test")
        expect:
            props.size() == 0
    }

    def "Test original Jedis factory"() {
        expect: underTest_.createInternalClient(new HostAndPort("localhost", 6379)) == null
    }

/**************************** DATA *******************************/
    String goodJson_ = """{
    "property": true,
    "type": "json"
}"""
    String badJson_ = """{
    "property": true ]
    "type": "json"
}"""
    String goodYaml_ = """---
property: true
type: yaml
"""
    String goodJsonSystem_ = """{
    "property": false,
    "type": "json",
    "overridden": {
        "key1": null,
        "key2": null
    },
    "new": "string"
}"""
    String goodJsonUser_ = """{
    "property": true,
    "overridden": {
        "key2": "overrode"
    }
}"""
    String goodYamlSystem_ = """---
property: false
type: yaml
overridden:
  key1: null
  key2: null
changed: string
"""
    String goodYamlUser_ = """---
property: true
type: yaml
overridden:
  key1: overrode
changed:
  - 1
  - 2
  - 3
new:
    object: value
"""
}
