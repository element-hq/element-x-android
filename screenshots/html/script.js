/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 import { screenshots } from './data.js';

const header = screenshots[0];

let visibleLanguages = header.map((x) => 1);
let imageWidth = 300;

function addForm() {
  // Insert the form into the div with id form_container
  const form = document.createElement('form');
  const languageLabel = document.createElement('label');
  languageLabel.textContent = 'Languages:';
  form.appendChild(languageLabel);
  // Add a check box per entry in the header
  for (let i = 0; i < header.length; i++){
    const label = document.createElement('label');
    const text = document.createTextNode(header[i]);
    const input = document.createElement('input');
    input.type = 'checkbox';
    input.disabled = i == 0;
    input.name = header[i];
    input.checked = visibleLanguages[i] == 1;
    input.onchange = (e) => {
      if (e.target.checked) {
        visibleLanguages[i] = 1;
      } else {
        visibleLanguages[i] = 0;
      }
      addTable();
    };
    label.appendChild(input);
    label.appendChild(text);
    form.appendChild(label);
  }
  // Add a label with the text "Width"
  const label = document.createElement('label');
  label.textContent = 'Screenshots width:';
  form.appendChild(label);
  // Add a input text to input the width of the image
  const widthInput = document.createElement('input');
  widthInput.id = 'width_input';
  widthInput.type = 'number';
  widthInput.min = 75;
  widthInput.max = 500;
  widthInput.step = 25;
  widthInput.value = imageWidth;
  widthInput.onchange = (e) => {
      imageWidth = e.target.value;
      addTable();
  };
  form.appendChild(widthInput);
  document.getElementById('form_container').appendChild(form);
}

function getNiceName(name) {
  var indices = [];
  for(var i = 0; i < name.length; i++) {
      if (name[i] === "_") indices.push(i);
  }
  return name.substring(indices[2] + 1, indices[3]);
}

function addTable() {
  // Remove any previous table
  document.getElementById('screenshots_container').innerHTML = '';
  // screenshots contains a table of screenshots, lets convert to an html table
  const table = document.createElement('table');
  const thead = document.createElement('thead');
  const tbody = document.createElement('tbody');

  // First item of screenshots contains the languages
  // Build the languages row
  const languagesHeaderRow = document.createElement('tr');
  for (let i = 0; i < header.length; i++) {
    // Do not add the language if it is hidden
    if (visibleLanguages[i] == 0) {
        continue;
    }
    const th = document.createElement('th');
    th.textContent = header[i];
    languagesHeaderRow.appendChild(th);
  }
  const numVisibleLanguages = languagesHeaderRow.childElementCount
  // Next items are the data
  var currentHeaderValue = "";
  for (let i = 1; i < screenshots.length; i++) {
    // Add a header for row, if different from previous
    let name = getNiceName(screenshots[i][0]);
    if(name != currentHeaderValue) {
      currentHeaderValue = name;
      const trHead = document.createElement('tr');
      const tdHead = document.createElement('td');
      tdHead.colSpan = numVisibleLanguages;
      tdHead.className = "view-header";
      tdHead.textContent = name;
      trHead.appendChild(tdHead);
      tbody.appendChild(trHead);
      tbody.appendChild(languagesHeaderRow.cloneNode(true));
    }
    const tr = document.createElement('tr');
    for (let j = 0; j < screenshots[i].length; j++) {
      if (visibleLanguages[j] == 0) {
        continue;
      }
      const td = document.createElement('td');
      let imageFile = screenshots[i][j];
      if (imageFile === '') {
        const text = document.createElement('p');
        text.textContent = 'No image';
        td.appendChild(text);
      } else {
        const img = document.createElement('img');
        img.className = "screenshot";
        img.src = `../${imageFile}`;
        img.title = imageFile;
        img.alt = "Missing image";
        img.width = imageWidth;
        td.appendChild(img);
      }
      tr.appendChild(td);
    }
    tbody.appendChild(tr);
  }
  table.appendChild(thead);
  table.appendChild(tbody);

  // Add the table to the div with id screenshots_container
  document.getElementById('screenshots_container').appendChild(table);
}

addForm();
addTable();
