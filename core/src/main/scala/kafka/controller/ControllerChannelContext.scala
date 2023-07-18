/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kafka.controller

import kafka.cluster.Broker
import org.apache.kafka.common.{TopicPartition, Uuid}

trait ControllerChannelContext {
  // Broker 啟動、刪除 Topic、Replica ReAssignment、sendRequestsToBrokers
  def isTopicDeletionInProgress(topicName: String): Boolean

  def topicIds: collection.Map[String, Uuid]


  // ControllerChannelManager.scala
  // AbstractControllerBrokerRequestBatch ->  addLeaderAndIsrRequestForBrokers -> 
  // 1. sendLeaderAndIsrRequest ->   metadataInstance.liveBrokerIdAndEpochs
  // 2. sendUpdateMetadataRequests
  // 3. sendStopReplicaRequests

  // KafkaController.scala 
  // processBrokerChange 從 zk 拉出 broker 的狀態(這邊要看) 
  // initializeControllerContext
  // doControlledShutdown
  // processBrokerChange
  // tryProcessAlterPartition
  // processAllocateProducerIds
  def liveBrokerIdAndEpochs: collection.Map[Int, Long]

  //////////////////////////////////////////////
  // ControllerChannelManager.scala
  // 1. sendLeaderAndIsrRequest
  // 2. sendUpdateMetadataRequests

  // KafkaController.scala 
  // initializeControllerContext
  // newFinalizedVersionOrIncompatibilityError
  def liveOrShuttingDownBrokers: collection.Set[Broker]

  // 各種操作
  def isTopicQueuedUpForDeletion(topic: String): Boolean


  // ControllerChannelContext.scala
  // addUpdateMetadataRequestForBrokers
  // 1. Eelection.scala -> leaderForOffline 判斷能否參選
  // KafkaController.scala 
  // delete
  def isReplicaOnline(brokerId: Int, partition: TopicPartition): Boolean

  def partitionReplicaAssignment(partition: TopicPartition): collection.Seq[Int]

  def leaderEpoch(topicPartition: TopicPartition): Int

  // 判斷 Broker 有無存活，這邊在 
  // 1. ReplicaStateMachine 內 ZkReplicaStateMachine 判斷 OfflineReplica 時會用到
  // 2. Controller ChannelManager addLeaderAndIsrRequestForBrokers() 會用到
  // 3. processAlterPartition ReassignPartitionsCommand 會用到
  // 4. processIsrChangeNotification
  // 5. processBrokerChange
  // 6. updateMetrics
  // 7. processControlledShutdown -> doControlledShutdown(with callback)
  // 8. initializeControllerContext
  // 9. onPartitionReassignment
  // 10. onReplicasBecomeOffline
  // 11. onBrokerUpdate
  // 12. onBrokerStartup
  // 13. readOffsetMessageValue
  def liveOrShuttingDownBrokerIds: collection.Set[Int]

  // 根據 Ctx 傳遞的 leader info 與 ISR 資訊
  def partitionLeadershipInfo(topicPartition: TopicPartition): Option[LeaderIsrAndControllerEpoch]
}
