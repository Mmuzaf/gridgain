#
# Copyright 2019 GridGain Systems, Inc. and Contributors.
# 
# Licensed under the GridGain Community Edition License (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""
Set of functions to manipulate caches.

Ignite `cache` can be viewed as a named entity designed to store key-value
pairs. Each cache is split transparently between different Ignite partitions.

The choice of `cache` term is due to historical reasons. (Ignite initially had
only non-persistent storage tier.)
"""

from typing import Union

from pyignite.datatypes.cache_config import cache_config_struct
from pyignite.datatypes.cache_properties import prop_map
from pyignite.datatypes import (
    Int, Byte, prop_codes, Short, String, StringArray,
)
from pyignite.queries import Query, ConfigQuery
from pyignite.queries.op_codes import *
from pyignite.utils import cache_id


def compact_cache_config(cache_config: dict) -> dict:
    """
    This is to make cache config read/write-symmetrical.

    :param cache_config: dict of cache config properties,
     like {'is_onheapcache_enabled': 1},
    :return: the same dict, but with property codes as keys,
     like {PROP_IS_ONHEAPCACHE_ENABLED: 1}.
    """
    result = {}
    for k, v in cache_config.items():
        if k == 'length':
            continue
        prop_code = getattr(prop_codes, 'PROP_{}'.format(k.upper()))
        result[prop_code] = v
    return result


def cache_get_configuration(
    connection: 'Connection', cache: Union[str, int], flags: int=0, query_id=None,
) -> 'APIResult':
    """
    Gets configuration for the given cache.

    :param connection: connection to Ignite server,
    :param cache: name or ID of the cache,
    :param flags: Ignite documentation is unclear on this subject,
    :param query_id: (optional) a value generated by client and returned as-is
     in response.query_id. When the parameter is omitted, a random value
     is generated,
    :return: API result data object. Result value is OrderedDict with
     the cache configuration parameters.
    """

    query_struct = Query(
        OP_CACHE_GET_CONFIGURATION,
        [
            ('hash_code', Int),
            ('flags', Byte),
        ],
        query_id=query_id,
    )
    result = query_struct.perform(
        connection,
        query_params={
            'hash_code': cache_id(cache),
            'flags': flags,
        },
        response_config=[
            ('cache_config', cache_config_struct),
        ],
    )
    if result.status == 0:
        result.value = compact_cache_config(result.value['cache_config'])
    return result


def cache_create(
    connection: 'Connection', name: str, query_id=None,
) -> 'APIResult':
    """
    Creates a cache with a given name. Returns error if a cache with specified
    name already exists.

    :param connection: connection to Ignite server,
    :param name: cache name,
    :param query_id: (optional) a value generated by client and returned as-is
     in response.query_id. When the parameter is omitted, a random value
     is generated,
    :return: API result data object. Contains zero status if a cache is
     created successfully, non-zero status and an error description otherwise.
    """

    query_struct = Query(
        OP_CACHE_CREATE_WITH_NAME,
        [
            ('cache_name', String),
        ],
        query_id=query_id,
    )
    return query_struct.perform(
        connection,
        query_params={
            'cache_name': name,
        },
    )


def cache_get_or_create(
    connection: 'Connection', name: str, query_id=None,
) -> 'APIResult':
    """
    Creates a cache with a given name. Does nothing if the cache exists.

    :param connection: connection to Ignite server,
    :param name: cache name,
    :param query_id: (optional) a value generated by client and returned as-is
     in response.query_id. When the parameter is omitted, a random value
     is generated,
    :return: API result data object. Contains zero status if a cache is
     created successfully, non-zero status and an error description otherwise.
    """

    query_struct = Query(
        OP_CACHE_GET_OR_CREATE_WITH_NAME,
        [
            ('cache_name', String),
        ],
        query_id=query_id,
    )
    return query_struct.perform(
        connection,
        query_params={
            'cache_name': name,
        },
    )


def cache_destroy(
    connection: 'Connection', cache: Union[str, int], query_id=None,
) -> 'APIResult':
    """
    Destroys cache with a given name.

    :param connection: connection to Ignite server,
    :param cache: name or ID of the cache,
    :param query_id: (optional) a value generated by client and returned as-is
     in response.query_id. When the parameter is omitted, a random value
     is generated,
    :return: API result data object.
    """

    query_struct = Query(
        OP_CACHE_DESTROY,[
            ('hash_code', Int),
        ],
        query_id=query_id,
    )
    return query_struct.perform(
        connection,
        query_params={
            'hash_code': cache_id(cache),
        },
    )


def cache_get_names(connection: 'Connection', query_id=None) -> 'APIResult':
    """
    Gets existing cache names.

    :param connection: connection to Ignite server,
    :param query_id: (optional) a value generated by client and returned as-is
     in response.query_id. When the parameter is omitted, a random value
     is generated,
    :return: API result data object. Contains zero status and a list of cache
     names, non-zero status and an error description otherwise.
    """

    query_struct = Query(OP_CACHE_GET_NAMES, query_id=query_id)
    result = query_struct.perform(
        connection,
        response_config=[
            ('cache_names', StringArray),
        ],
    )
    if result.status == 0:
        result.value = result.value['cache_names']
    return result


def cache_create_with_config(
    connection: 'Connection', cache_props: dict, query_id=None,
) -> 'APIResult':
    """
    Creates cache with provided configuration. An error is returned
    if the name is already in use.

    :param connection: connection to Ignite server,
    :param cache_props: cache configuration properties to create cache with
     in form of dictionary {property code: python value}.
     You must supply at least name (PROP_NAME),
    :param query_id: (optional) a value generated by client and returned as-is
     in response.query_id. When the parameter is omitted, a random value
     is generated,
    :return: API result data object. Contains zero status if cache was created,
     non-zero status and an error description otherwise.
    """

    prop_types = {}
    prop_values = {}
    for i, prop_item in enumerate(cache_props.items()):
        prop_code, prop_value = prop_item
        prop_name = 'property_{}'.format(i)
        prop_types[prop_name] = prop_map(prop_code)
        prop_values[prop_name] = prop_value
    prop_values['param_count'] = len(cache_props)

    query_struct = ConfigQuery(
        OP_CACHE_CREATE_WITH_CONFIGURATION,
        [
            ('param_count', Short),
        ] + list(prop_types.items()),
        query_id=query_id,
    )
    return query_struct.perform(connection, query_params=prop_values)


def cache_get_or_create_with_config(
    connection: 'Connection', cache_props: dict, query_id=None,
) -> 'APIResult':
    """
    Creates cache with provided configuration. Does nothing if the name
    is already in use.

    :param connection: connection to Ignite server,
    :param cache_props: cache configuration properties to create cache with
     in form of dictionary {property code: python value}.
     You must supply at least name (PROP_NAME),
    :param query_id: (optional) a value generated by client and returned as-is
     in response.query_id. When the parameter is omitted, a random value
     is generated,
    :return: API result data object. Contains zero status if cache was created,
     non-zero status and an error description otherwise.
    """

    prop_types = {}
    prop_values = {}
    for i, prop_item in enumerate(cache_props.items()):
        prop_code, prop_value = prop_item
        prop_name = 'property_{}'.format(i)
        prop_types[prop_name] = prop_map(prop_code)
        prop_values[prop_name] = prop_value
    prop_values['param_count'] = len(cache_props)

    query_struct = ConfigQuery(
        OP_CACHE_GET_OR_CREATE_WITH_CONFIGURATION,
        [
            ('param_count', Short),
        ] + list(prop_types.items()),
        query_id=query_id,
    )
    return query_struct.perform(connection, query_params=prop_values)
