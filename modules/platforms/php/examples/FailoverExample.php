<?php
/*
 *                   GridGain Community Edition Licensing
 *                   Copyright 2019 GridGain Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License") modified with Commons Clause
 * Restriction; you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * 
 * Commons Clause Restriction
 * 
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 * 
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including without
 * limitation fees for hosting or consulting/ support services related to the Software), a product or
 * service whose value derives, entirely or substantially, from the functionality of the Software.
 * Any license notice or attribution required by the License must also include this Commons Clause
 * License Condition notice.
 * 
 * For purposes of the clause above, the “Licensor” is Copyright 2019 GridGain Systems, Inc.,
 * the “License” is the Apache License, Version 2.0, and the Software is the GridGain Community
 * Edition software provided with this notice.
 */

require_once __DIR__ . '/../vendor/autoload.php';

use Apache\Ignite\Client;
use Apache\Ignite\ClientConfiguration;
use Apache\Ignite\Exception\ClientException;
use Apache\Ignite\Exception\NoConnectionException;
use Apache\Ignite\Exception\OperationStatusUnknownException;

const ENDPOINT1 = 'localhost:10800';
const ENDPOINT2 = 'localhost:10801';
const ENDPOINT3 = 'localhost:10802';

const CACHE_NAME = 'test_cache';

// This example demonstrates failover behavior of the client
// - configures the client to connect to a set of nodes
// - connects to a node
// - executes an operation with Ignite server in a cycle (10 operations with 5 seconds pause) and finishes
// - if connection is broken, the client automatically tries to reconnect to another node
// - if not possible to connect to any nodes, the example finishes
function connectClient() {
    $client = new Client();
    $client->setDebug(true);
    try {
        $clientConfiguration = new ClientConfiguration(ENDPOINT1, ENDPOINT2, ENDPOINT3);
        // connect to Ignite node
        $client->connect($clientConfiguration);
        echo("Client connected successfully" . PHP_EOL);
        for ($i = 0; $i < 10; $i++) {
            try {
                $client->getOrCreateCache(CACHE_NAME);
                sleep(5);
            } catch (OperationStatusUnknownException $e) {
                // Status of the operation is unknown,
                // a real application may repeat the operation if necessary
            }
        }
    } catch (NoConnectionException $e) {
        // The client is disconnected, all further operation with Ignite server fail till the client is connected again.
        // A real application may recall $client->connect() with the same or different list of Ignite nodes.
        echo('ERROR: ' . $e->getMessage() . PHP_EOL);
    } catch (ClientException $e) {
        echo('ERROR: ' . $e->getMessage() . PHP_EOL);
    } finally {
        $client->disconnect();
    }
}

connectClient();
