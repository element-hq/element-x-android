#! /bin/bash
#
# Copyright (c) 2023 New Vector Ltd
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Format is:

# Error
# adb shell am start -a android.intent.action.VIEW -d "io.element:/callback?error=access_denied\\&state=IFF1UETGye2ZA8pO"

# Success
adb shell am start -a android.intent.action.VIEW -d "io.element:/callback?state=IFF1UETGye2ZA8pO\\&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
