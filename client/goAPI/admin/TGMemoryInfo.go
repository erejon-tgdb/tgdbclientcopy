package admin

/**
 * Copyright 2018-19 TIBCO Software Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use this file except
 * in compliance with the License.
 * A copy of the License is included in the distribution package with this file.
 * You also may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF DirectionAny KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File name: TGMemoryInfo.go
 * Created on: Apr 06, 2019
 * Created by: achavan
 * SVN id: $id: $
 *
 */

// TGMemoryInfo allows users to retrieve the memory information from server
type TGMemoryInfo interface {
	// GetFreeMemory returns the free memory size from server
	GetFreeMemory() int64
	// GetMaxMemory returns the max memory size from server
	GetMaxMemory() int64
	// GetSharedMemoryFileLocation returns the shared memory file location
	GetSharedMemoryFileLocation() string
	// GetUsedMemory returns the used memory size from server
	GetUsedMemory() int64
}
