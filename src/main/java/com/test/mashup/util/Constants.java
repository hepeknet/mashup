package com.test.mashup.util;

/**
 * Constants used by our application
 * 
 * @author borisa
 *
 */
public interface Constants {

	String CONFIGURATION_LOCATION_SYS_PROPERTY_NAME = "mashup.config.location";

	String GITHUB_SEARCH_BASE_URL_PROPERTY_NAME = "github_search_base_url";

	String GITHUB_SEARCH_DEFAULT_SORT_FIELD_PROPERTY_NAME = "github_search_default_sort_field_name";

	String TWITTER_SEARCH_BASE_URL_PROPERTY_NAME = "twitter_search_base_url";

	String TWITTER_BEARER_URL_PROPERTY_NAME = "twitter_bearer_url";

	String TWITTER_AUTH_KEY_PROPERTY_NAME = "twitter_auth_key";

	String TWITTER_AUTH_SECRET_PROPERTY_NAME = "twitter_auth_secret";

	String TWITTER_BEARER_REQUIRED_TOKEN_TYPE_PROPERTY_NAME = "twitter_bearer_required_token_type";

	String TWITTER_SEARCH_MAX_TWEETS_PROPERTY_NAME = "twitter_search_max_tweets_per_search";

	String GITHUB_SEARCH_MAX_PROJECTS_LIMIT_PROPERTY_NAME = "github_search_max_projects_per_search_limit";

	String LOCAL_CACHE_MAX_SIZE_PROPERTY_NAME = "local_cache_max_size";

	String GITHUB_SEARCH_CACHE_TIMEOUT_SECONDS_PROPERTY_NAME = "github_search_cache_timeout_seconds";

	String TWITTER_SEARCH_THREAD_POOL_SIZE_PROPERTY_NAME = "twitter_search_thread_pool_size";

	String TWITTER_SEARCH_RETRY_MAX_ATTEMPTS_PROPERTY_NAME = "twitter_search_retry_max_attempts";

	String TWITTER_SEARCH_RETRY_FIXED_BACKOFF_MILLIS_PROPERTY_NAME = "twitter_search_retry_fixed_backoff_millis";

	String GITHUB_SEARCH_RETRY_MAX_ATTEMPTS_PROPERTY_NAME = "github_search_retry_max_attemps";

	String GITHUB_SEARCH_RETRY_FIXED_BACKOFF_MILLIS_PROPERTY_NAME = "github_search_retry_fixed_backoff_millis";

}
