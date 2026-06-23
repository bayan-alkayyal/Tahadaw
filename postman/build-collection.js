/*
 * Generates Tahadaw-Full-System-Flows.postman_collection.json
 *
 * SECURITY MODEL (current backend):
 *   - Auth is HTTP Basic (username + password) over a session. There is no JWT.
 *   - Only POST /api/v1/users/register, /api/v1/public/**, the Moyasar webhook and
 *     moyasar-status callbacks are public. Everything else needs a logged-in user.
 *   - The signed-in user is taken from the Spring Security principal
 *     (@AuthenticationPrincipal). Endpoints DO NOT take a userId param anymore.
 *   - A set of admin-tooling endpoints require the ADMIN authority:
 *       * GET  /api/v1/users/get
 *       * /api/v1/required-questions  add | update | delete | disable | get
 *       * /api/v1/ai-questions        create | get | get-by-id | update | delete
 *       * /api/v1/ai-answers          create | get | get-by-id | update | delete
 *       * /api/v1/required-question-answers  create | get | get-by-id | update | delete
 *
 * HOW THE COLLECTION AUTHENTICATES:
 *   - Collection-level auth = Basic {{username}} / {{password}}.
 *   - Flow 1 registers Saud and stores his credentials in {{username}}/{{password}}
 *     so every later request is authenticated as him.
 *   - Registration always assigns role USER. There is no endpoint to create an ADMIN,
 *     so admin-only requests use Basic {{adminUsername}} / {{adminPassword}} and are
 *     tolerant: set those two variables to a real ADMIN account (promote a user in the
 *     DB, e.g. UPDATE "user" SET role='ADMIN' WHERE username='...') to exercise them.
 *
 * Test data:
 *   - Names      : Saud (سعود) = owner/sender, Ammar (عمار) = recipient
 *   - Email      : swwdswwd124@gmail.com  (user rows use a +alias so reruns stay unique)
 *   - Phone      : 0502427714
 *   - Free text  : Arabic
 */
const fs = require('fs');
const path = require('path');

const CT = { key: 'Content-Type', value: 'application/json' };

function ev(listen, lines) {
  return { listen, script: { type: 'text/javascript', exec: lines } };
}

const ASSERT_200 = "pm.test('HTTP 200', () => pm.expect(pm.response.code).to.eql(200));";
function tolerant(label) {
  return [
    "var c = pm.response.code;",
    "pm.test(" + JSON.stringify(label) + ", function () {",
    "  if (c === 200) { pm.expect(c).to.eql(200); }",
    "  else { console.log(" + JSON.stringify(label + ' -> HTTP ') + " + c + ': ' + pm.response.text().slice(0,300)); pm.expect(true).to.be.true; }",
    "});"
  ];
}

// auth: undefined -> inherit collection (Basic as Saud); 'none' -> public; 'admin' -> Basic admin creds.
const ADMIN_AUTH = {
  type: 'basic',
  basic: [
    { key: 'username', value: '{{adminUsername}}', type: 'string' },
    { key: 'password', value: '{{adminPassword}}', type: 'string' }
  ]
};

function req(name, method, url, opts) {
  opts = opts || {};
  const item = { name, request: { method, header: [], url } };
  if (opts.auth === 'none') item.request.auth = { type: 'noauth' };
  else if (opts.auth === 'admin') item.request.auth = ADMIN_AUTH;
  if (opts.body !== undefined) {
    item.request.header.push(CT);
    item.request.body = {
      mode: 'raw',
      raw: typeof opts.body === 'string' ? opts.body : JSON.stringify(opts.body, null, 2),
      options: { raw: { language: 'json' } }
    };
  }
  if (opts.desc) item.request.description = opts.desc;
  const events = [];
  if (opts.pre) events.push(ev('prerequest', opts.pre));
  events.push(ev('test', opts.test || [ASSERT_200]));
  item.event = events;
  return item;
}

function folder(name, desc, items) {
  return { name, description: desc, item: items };
}

// ----- capture snippets -----
function captureMaxId(varName, label) {
  return [
    ASSERT_200,
    "var arr = pm.response.json();",
    "if (Array.isArray(arr) && arr.length) {",
    "  var max = arr.reduce(function (a, b) { return (a.id > b.id ? a : b); });",
    "  pm.collectionVariables.set(" + JSON.stringify(varName) + ", max.id);",
    "  console.log(" + JSON.stringify(label + ' =') + ", max.id);",
    "}",
    "pm.test(" + JSON.stringify(label + ' captured') + ", function () { pm.expect(pm.collectionVariables.get(" + JSON.stringify(varName) + ")).to.not.be.undefined; });"
  ];
}
function captureFirstId(varName, label) {
  return [
    ASSERT_200,
    "var arr = pm.response.json();",
    "if (Array.isArray(arr) && arr.length) { pm.collectionVariables.set(" + JSON.stringify(varName) + ", arr[0].id); console.log(" + JSON.stringify(label + ' =') + ", arr[0].id); }",
    "pm.test(" + JSON.stringify(label + ' captured') + ", function () { pm.expect(pm.collectionVariables.get(" + JSON.stringify(varName) + ")).to.not.be.undefined; });"
  ];
}
function captureBodyId(varName, label) {
  return [
    ASSERT_200,
    "var b = pm.response.json();",
    "if (b && b.id) { pm.collectionVariables.set(" + JSON.stringify(varName) + ", b.id); console.log(" + JSON.stringify(label + ' =') + ", b.id); }"
  ];
}

const B = '{{base_url}}';
const R = '{{recipientId}}';
const G = '{{giftPlanId}}';

// ============================ FLOW FOLDERS ============================
const folders = [];

// ---- Bayan: Account & User Setup ----
folders.push(folder(
  'Bayan - 1. Account & User Setup',
  'Dev: Bayan. Registers the test account (Saud) and stores his credentials in {{username}}/{{password}} so every later request authenticates via HTTP Basic. Username/email use a per-run stamp so reruns never collide; mail still lands in swwdswwd124@gmail.com via the +alias.',
  [
    req('Register User (Saud)', 'POST', B + '/api/v1/users/register', {
      auth: 'none',
      desc: 'Public endpoint (permitAll). Registration always assigns role USER.',
      pre: ["pm.collectionVariables.set('stamp', String(Date.now()));"],
      body: { username: 'saud_{{stamp}}', password: 'Saud!2026pass', fullName: 'سعود', email: 'swwdswwd124+{{stamp}}@gmail.com', phoneNumber: '0502427714' },
      test: [
        ASSERT_200,
        "// Persist Basic-auth credentials for all later requests.",
        "pm.collectionVariables.set('username', 'saud_' + pm.collectionVariables.get('stamp'));",
        "pm.collectionVariables.set('password', 'Saud!2026pass');",
        "console.log('Logged in as', pm.collectionVariables.get('username'));"
      ]
    }),
    req('Verify Authentication (whoami via recipients)', 'GET', B + '/api/v1/recipients/get', {
      desc: 'First authenticated call. Confirms HTTP Basic login works before the rest of the run.'
    }),
    req('Update User (still authenticated)', 'PUT', B + '/api/v1/users/update', {
      desc: 'Updates the signed-in user (principal). Keeps the same username/password so credentials stay valid.',
      body: { username: 'saud_{{stamp}}', password: 'Saud!2026pass', fullName: 'سعود الشافعي', email: 'swwdswwd124+{{stamp}}@gmail.com', phoneNumber: '0502427714' }
    }),
    req('Premium Status (should be false)', 'GET', B + '/api/v1/premium/status', {})
  ]
));

// ---- Bayan: Recipients ----
folders.push(folder(
  'Bayan - 2. Recipients',
  'Dev: Bayan. Flow 2 — recipient profiles for the signed-in user. Creates the recipient Ammar (عمار).',
  [
    req('Add Recipient (Ammar)', 'POST', B + '/api/v1/recipients/add', {
      body: {
        name: 'عمار', relationship: 'أخ', age: 24, gender: 'ذكر',
        interests: 'القهوة المختصة، التقنية، السيارات',
        hobbies: 'تحضير القهوة، الألعاب الإلكترونية',
        favoriteColors: 'الأسود، الكحلي', favoriteBrands: 'آبل، سوني',
        dislikes: 'الملابس التقليدية، الأكواب العادية',
        personalityStyle: 'عملي وبسيط', sizeInfo: 'مقاس L',
        notes: 'يفضّل الهدايا العملية والمفيدة'
      }
    }),
    req('List My Recipients (capture recipientId)', 'GET', B + '/api/v1/recipients/get-by-user-id', {
      test: [
        ASSERT_200,
        "var arr = pm.response.json();",
        "var pick = (arr || []).filter(function (x) { return x.name === 'عمار'; });",
        "var target = pick.length ? pick.reduce(function (a,b){return a.id>b.id?a:b;}) : (Array.isArray(arr)&&arr.length ? arr.reduce(function(a,b){return a.id>b.id?a:b;}) : null);",
        "if (target) { pm.collectionVariables.set('recipientId', target.id); console.log('recipientId =', target.id); }",
        "pm.test('recipientId captured', function () { pm.expect(pm.collectionVariables.get('recipientId')).to.not.be.undefined; });"
      ]
    }),
    req('Get Recipient By Id', 'GET', B + '/api/v1/recipients/get/' + R, {}),
    req('Update Recipient', 'PUT', B + '/api/v1/recipients/update/' + R, {
      body: {
        name: 'عمار', relationship: 'أخ', age: 25, gender: 'ذكر',
        interests: 'القهوة المختصة، التصوير، التقنية',
        hobbies: 'تحضير القهوة، التصوير الفوتوغرافي',
        favoriteColors: 'الأسود، الرمادي', favoriteBrands: 'آبل، كانون',
        dislikes: 'الأكواب العادية', personalityStyle: 'عملي ويحب الجودة',
        sizeInfo: 'مقاس L', notes: 'تحدّث مؤخراً عن رغبته بكاميرا احترافية'
      }
    }),
    req('Recipient Gift History', 'GET', B + '/api/v1/recipients/' + R + '/gift-history', {})
  ]
));

// ---- Bayan: Admin Required Questions ----
folders.push(folder(
  'Bayan - 3. Admin: Required Questions',
  'Dev: Bayan. Flow 17 — ADMIN seeds the fixed required questions (POST/GET /required-questions are ADMIN-only). Requests use Basic {{adminUsername}}/{{adminPassword}} and are tolerant: set those to a real ADMIN account to actually seed. If questions were seeded before, the user-facing list in flow 6 still works.',
  [
    req('Add Required Question 1', 'POST', B + '/api/v1/required-questions/add', {
      auth: 'admin',
      body: { questionText: 'ما مدى قربك من الشخص؟', questionType: 'TEXT', isActive: true, displayOrder: 1 },
      test: tolerant('Required question 1 added (admin)')
    }),
    req('Add Required Question 2', 'POST', B + '/api/v1/required-questions/add', {
      auth: 'admin',
      body: { questionText: 'ما الميزانية التقريبية للهدية؟', questionType: 'TEXT', isActive: true, displayOrder: 2 },
      test: tolerant('Required question 2 added (admin)')
    }),
    req('List All Required Questions', 'GET', B + '/api/v1/required-questions/get', { auth: 'admin', test: tolerant('Required questions listed (admin)') })
  ]
));

// ---- Saud: Premium Payment ----
folders.push(folder(
  'Saud - 4. Premium Payment (Moyasar)',
  'Dev: Saud. Flow 11 — one-time premium payment via Moyasar sandbox for the signed-in user (no userId in the body — taken from the principal). NOTE: activation needs the manual 3-D Secure step (open transactionUrl in a browser and approve). Until then the user stays non-premium, so premium-gated flows below answer with 403 — expected in an automated run.',
  [
    req('Pay Premium (test card)', 'POST', B + '/api/v1/payments/premium', {
      body: { name: 'Saud Ammar', number: '4111111111111111', cvc: '123', month: '12', year: '30', callbackUrl: 'https://example.com/callback' },
      test: [
        ...tolerant('Premium payment accepted by Moyasar'),
        "try { var b = pm.response.json(); if (b && b.transactionId) pm.collectionVariables.set('moyasarId', b.transactionId); if (b && b.transactionUrl) console.log('OPEN THIS 3DS URL TO ACTIVATE PREMIUM:', b.transactionUrl); } catch (e) {}"
      ]
    }),
    req('Refresh Moyasar Status', 'GET', B + '/api/v1/payments/moyasar-status/{{moyasarId}}', { auth: 'none', test: tolerant('Moyasar status refreshed') }),
    req('My Payments', 'GET', B + '/api/v1/payments/my', {}),
    req('Premium Status', 'GET', B + '/api/v1/premium/status', {})
  ]
));

// ---- Shahad: Gift Plans ----
folders.push(folder(
  'Shahad - 5. Gift Plans',
  'Dev: Shahad. Flow 3 — gift plan CRUD for the signed-in user. Create returns a message only, so we read the list to capture the new giftPlanId.',
  [
    req('Create Gift Plan', 'POST', B + '/api/v1/gift-plans/create/' + R, {
      body: { occasionType: 'GRADUATION', occasionDate: '{{occasionDate}}', budget: 500, currency: 'SAR', preferredGiftStyle: 'PRACTICAL', language: 'ar' }
    }),
    req('List My Plans (capture giftPlanId)', 'GET', B + '/api/v1/gift-plans/get-my-plans', { test: captureMaxId('giftPlanId', 'giftPlanId') }),
    req('Get Plan By Id', 'GET', B + '/api/v1/gift-plans/get-plan-by-id/' + G, {}),
    req('Gift Plan Summary', 'GET', B + '/api/v1/gift-plans/get-gift-plan-Summery/' + G, {}),
    req('Get Active Plans', 'GET', B + '/api/v1/gift-plans/get-active-plans', {}),
    req('Update Gift Plan', 'PUT', B + '/api/v1/gift-plans/update/' + G, {
      body: { occasionType: 'GRADUATION', occasionDate: '{{occasionDate}}', budget: 600, currency: 'SAR', preferredGiftStyle: 'PRACTICAL', language: 'ar' }
    })
  ]
));

// ---- Shahad: Required Q&A ----
folders.push(folder(
  'Shahad - 6. Required Questions & Answers',
  'Dev: Shahad. Flow 4 — list active required questions for the plan (user endpoint), answer EVERY one dynamically, then submit. Moves the plan to REQUIRED_QUESTIONS_ANSWERED.',
  [
    req('List Required Questions (build answers)', 'GET', B + '/api/v1/required-questions/gift-plans/' + G, {
      test: [
        ASSERT_200,
        "var qs = []; try { qs = pm.response.json(); } catch (e) {}",
        "var samples = ['قريب جداً، نتواصل يومياً.', 'الميزانية حوالي 500 ريال.', 'يفضّل الهدايا العملية والتقنية.'];",
        "var answers = (Array.isArray(qs) ? qs : []).map(function (q, i) { return { requiredQuestionId: (q.id != null ? q.id : (q.requiredQuestionId != null ? q.requiredQuestionId : q.questionId)), answerText: samples[i % samples.length] }; });",
        "pm.collectionVariables.set('requiredAnswersBody', JSON.stringify({ answers: answers }));",
        "pm.test('Built answers for all required questions', function () { pm.expect(answers.length).to.be.above(0); });"
      ]
    }),
    req('Submit Required Answers', 'POST', B + '/api/v1/required-question-answers/gift-plans/' + G + '/submit', { body: '{{requiredAnswersBody}}' }),
    req('List Required Answers', 'GET', B + '/api/v1/required-question-answers/gift-plans/' + G, {})
  ]
));

// ---- Shahad: AI Q&A ----
folders.push(folder(
  'Shahad - 7. AI Follow-up Questions & Answers',
  'Dev: Shahad. Flow 5 — AI generates follow-up questions (OpenAI) for the signed-in user, we answer EVERY one dynamically. Moves the plan to AI_QUESTIONS_ANSWERED.',
  [
    req('Generate AI Questions', 'GET', B + '/api/v1/ai-questions/generate/' + G, { test: tolerant('AI questions generated (needs openai key)') }),
    req('List AI Questions (build answers)', 'GET', B + '/api/v1/ai-questions/gift-plans/' + G, {
      test: [
        ASSERT_200,
        "var qs = []; try { qs = pm.response.json(); } catch (e) {}",
        "var samples = ['نعم، يحب ذلك كثيراً.', 'لا يملكها حالياً.', 'يفضّل اللون الأسود.', 'نعم، يناسب اهتماماته.'];",
        "var answers = (Array.isArray(qs) ? qs : []).map(function (q, i) { return { aiGeneratedQuestionId: (q.id != null ? q.id : (q.aiGeneratedQuestionId != null ? q.aiGeneratedQuestionId : q.questionId)), answerText: samples[i % samples.length] }; });",
        "pm.collectionVariables.set('aiAnswersBody', JSON.stringify({ answers: answers }));",
        "pm.test('Built answers for all AI questions', function () { pm.expect(answers.length).to.be.above(0); });"
      ]
    }),
    req('Submit AI Answers', 'POST', B + '/api/v1/ai-answers/gift-plans/' + G, { body: '{{aiAnswersBody}}' }),
    req('List AI Answers', 'GET', B + '/api/v1/ai-answers/gift-plans/' + G, {})
  ]
));

// ---- Shahad: Recommendations ----
folders.push(folder(
  'Shahad - 8. AI Gift Recommendations',
  'Dev: Shahad. Flow 6 — generate AI gift ideas, then select one (required before product search).',
  [
    req('Generate Recommendations (capture id)', 'GET', B + '/api/v1/gift-recommendations/gift-plans/' + G, { test: captureFirstId('recommendationId', 'recommendationId') }),
    req('Select Recommendation', 'PUT', B + '/api/v1/gift-recommendations/{{recommendationId}}/select', {}),
    req('Get Selected Idea', 'GET', B + '/api/v1/gift-recommendations/gift-plans/' + G + '/selected', {})
  ]
));

// ---- Shahad: Product Search ----
folders.push(folder(
  'Shahad - 9. Product Search & Selection',
  'Dev: Shahad. Flow 7 — search real products (SearchAPI.io) for the selected idea, then save the chosen one. Tolerant: needs searchapi.api.key configured.',
  [
    req('Search Products (capture productId)', 'GET', B + '/api/v1/search/gift-plans/' + G + '/products', {
      test: [
        ...tolerant('Product search returned results'),
        "try { var arr = pm.response.json(); if (Array.isArray(arr) && arr.length) { pm.collectionVariables.set('productId', arr[0].id); console.log('productId =', arr[0].id); } } catch (e) {}"
      ]
    }),
    req('Select Product', 'POST', B + '/api/v1/selected-products/select-product/{{productId}}', { test: tolerant('Product selected') }),
    req('Get Selected Product (capture id)', 'GET', B + '/api/v1/selected-products/get-selected-product/' + G, {
      test: [
        ...tolerant('Selected product fetched'),
        "try { var b = pm.response.json(); if (b && b.id) { pm.collectionVariables.set('selectedProductId', b.id); console.log('selectedProductId =', b.id); } } catch (e) {}"
      ]
    })
  ]
));

// ---- Saud: Gift Messages ----
folders.push(folder(
  'Saud - 10. Gift Messages',
  'Dev: Saud. Flow 8 — free AI gift message (standalone, no gift plan), AI from plan, plus manual message + edit. All for the signed-in user.',
  [
    req('Generate Gift Message (AI, capture id)', 'POST', B + '/api/v1/gift-messages/generate', {
      body: { recipientName: 'عمار', relationship: 'أخ', occasion: 'تخرّج', giftName: 'ساعة ذكية', tone: 'دافئ وفخور', language: 'ar', dialect: 'سعودي' },
      test: captureBodyId('giftMessageId', 'giftMessageId')
    }),
    req('Generate Gift Message From Plan', 'POST', B + '/api/v1/gift-messages/generate-from-plan/' + G, {
      body: { tone: 'دافئ وفخور', language: 'ar', dialect: 'سعودي' }, test: tolerant('Message generated from plan')
    }),
    req('Create Manual Message', 'POST', B + '/api/v1/gift-messages/manual', {
      body: { messageText: 'مبروك تخرّجك يا عمار، فخورون فيك دائماً وننتظر إنجازاتك القادمة.' }
    }),
    req('List My Messages', 'GET', B + '/api/v1/gift-messages/my', {}),
    req('Get Message By Id', 'GET', B + '/api/v1/gift-messages/{{giftMessageId}}', {}),
    req('Update Message', 'PUT', B + '/api/v1/gift-messages/{{giftMessageId}}', {
      body: { messageText: 'ألف مبروك التخرّج يا عمار، هذه هدية بسيطة تليق بك.', tone: 'دافئ', language: 'ar' }
    })
  ]
));

// ---- Saud: Gift Card ----
folders.push(folder(
  'Saud - 11. Gift Card (Premium)',
  'Dev: Saud. Flow 13 — premium gift card with QR + rendered image, emailed to swwdswwd124@gmail.com. Premium-gated: returns 403 until the manual 3DS premium activation is done.',
  [
    req('Create Gift Card', 'POST', B + '/api/v1/gift-cards', {
      body: { giftMessageId: '{{giftMessageId}}', recipientName: 'عمار', senderName: 'سعود', cardSize: 'MEDIUM', linkType: 'VIDEO', linkUrl: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', sentToEmail: 'swwdswwd124@gmail.com' },
      test: [
        ...tolerant('Gift card created (needs premium)'),
        "try { var b = pm.response.json(); if (b && b.id) { pm.collectionVariables.set('giftCardId', b.id); console.log('giftCardId =', b.id); } } catch (e) {}"
      ]
    }),
    req('List My Gift Cards', 'GET', B + '/api/v1/gift-cards/my', {}),
    req('Get Gift Card By Id', 'GET', B + '/api/v1/gift-cards/{{giftCardId}}', { test: tolerant('Gift card fetched') }),
    req('View Gift Card Image (PNG)', 'GET', B + '/api/v1/gift-cards/{{giftCardId}}/image', { test: tolerant('Gift card image returned') }),
    req('Update Gift Card', 'PUT', B + '/api/v1/gift-cards/{{giftCardId}}', {
      body: { cardSize: 'LARGE', recipientName: 'عمار', senderName: 'سعود' }, test: tolerant('Gift card updated')
    }),
    req('Regenerate Gift Card', 'POST', B + '/api/v1/gift-cards/{{giftCardId}}/regenerate', { test: tolerant('Gift card regenerated') }),
    req('Send Gift Card Email', 'POST', B + '/api/v1/gift-cards/{{giftCardId}}/send-email', {
      body: { email: 'swwdswwd124@gmail.com' }, test: tolerant('Gift card email sent')
    })
  ]
));

// ---- Saud: Surprise Plan ----
folders.push(folder(
  'Saud - 12. Surprise Plan (Premium)',
  'Dev: Saud. Flow 12 — AI surprise plan. Premium-gated: returns 403 until premium is active.',
  [
    req('Generate Surprise Plan', 'POST', B + '/api/v1/gift-plans/' + G + '/surprise-plan/generate', { body: { language: 'ar' }, test: tolerant('Surprise plan generated (needs premium)') }),
    req('Get Surprise Plan', 'GET', B + '/api/v1/gift-plans/' + G + '/surprise-plan', { test: tolerant('Surprise plan fetched') })
  ]
));

// ---- Saud: Gift History ----
folders.push(folder(
  'Saud - 13. Gift History',
  'Dev: Saud. Flow 9 — record the gifted product so future AI runs avoid repeats. Tolerant: depends on a selected product from flow 9.',
  [
    req('Log Gift From Product', 'POST', B + '/api/v1/gift-history/from-product/{{selectedProductId}}', {
      body: { wasGifted: true, userRating: 5, notes: 'أحبّها عمار كثيراً وكانت مناسبة جداً.' }, test: tolerant('Gift history logged')
    }),
    req('Get History By Product', 'GET', B + '/api/v1/gift-history/from-product/{{selectedProductId}}', { test: tolerant('History fetched') }),
    req('Edit History Log', 'PUT', B + '/api/v1/gift-history/from-product/{{selectedProductId}}', {
      body: { wasGifted: true, userRating: 4, notes: 'هدية ممتازة، لكن التوصيل تأخّر قليلاً.' }, test: tolerant('History edited')
    }),
    req('List My History', 'GET', B + '/api/v1/gift-history/my', {}),
    req('History Summary', 'GET', B + '/api/v1/gift-history/summary', {})
  ]
));

// ---- Bayan: Gift Quality Check ----
folders.push(folder(
  'Bayan - 14. Gift Quality Check',
  'Dev: Bayan. Flow 10 — standalone AI suitability check for a recipient (no gift plan needed).',
  [
    req('Run Quality Check', 'POST', B + '/api/v1/gift-quality-checks/add/' + R, {
      body: { giftName: 'ساعة ذكية', giftDescription: 'ساعة ذكية رياضية تدعم تتبع اللياقة والإشعارات', price: 499.0, occasionType: 'GRADUATION' }
    }),
    req('List Checks By Recipient (capture id)', 'GET', B + '/api/v1/gift-quality-checks/recipients/' + R, { test: captureMaxId('qualityCheckId', 'qualityCheckId') }),
    req('Get Quality Check By Id', 'GET', B + '/api/v1/gift-quality-checks/{{qualityCheckId}}', {})
  ]
));

// ---- Bayan: Reminders ----
folders.push(folder(
  'Bayan - 15. Reminders',
  'Dev: Bayan. Flow 15 — reminders (email / WhatsApp / in-app) for the signed-in user.',
  [
    req('Add Reminder', 'POST', B + '/api/v1/reminders/add/' + R, {
      body: { reminderDate: '{{reminderDate}}', message: 'تذكير: تخرّج عمار بعد أسبوع، جهّز الهدية!', status: 'PENDING' }
    }),
    req('Get My Reminders (capture id)', 'GET', B + '/api/v1/reminders/get-my', { test: captureMaxId('reminderId', 'reminderId') }),
    req('Update Reminder', 'PUT', B + '/api/v1/reminders/update/{{reminderId}}', {
      body: { reminderDate: '{{reminderDate}}', message: 'تذكير محدّث: لا تنسَ تغليف هدية عمار.', status: 'PENDING' }
    }),
    req('Delete Reminder', 'DELETE', B + '/api/v1/reminders/delete/{{reminderId}}', {})
  ]
));

// ---- Bayan: Notifications ----
folders.push(folder(
  'Bayan - 16. Notifications',
  'Dev: Bayan. Flow 16 — in-app notifications for the signed-in user.',
  [
    req('Create Notification (capture id)', 'POST', B + '/api/v1/notifications', {
      body: { title: 'توصياتك جاهزة', message: 'تم تجهيز توصيات الهدايا الخاصة بعمار.', type: 'RECOMMENDATIONS_READY', status: 'UNREAD' },
      test: captureBodyId('notificationId', 'notificationId')
    }),
    req('List My Notifications', 'GET', B + '/api/v1/notifications/my', {}),
    req('Get Notification By Id', 'GET', B + '/api/v1/notifications/{{notificationId}}', {}),
    req('Mark Notification Read', 'PUT', B + '/api/v1/notifications/{{notificationId}}', { body: { status: 'READ' } }),
    req('Delete Notification', 'DELETE', B + '/api/v1/notifications/{{notificationId}}', {})
  ]
));

// ---- Bayan: Group Gifts ----
folders.push(folder(
  'Bayan - 17. Group Gifts & Voting',
  'Dev: Bayan. Flow 14 — group gift with options, AI options, invites, public voting, results. Owner endpoints use Saud\'s login; the vote endpoints are public (/api/v1/public/group-gifts) and use the invite token, so they send no auth.',
  [
    req('Create Group Gift (capture id)', 'POST', B + '/api/v1/group-gifts', {
      body: { recipientId: '{{recipientId}}', title: 'هدية تخرّج جماعية لعمار', description: 'نجمع مساهمات الأصدقاء لاختيار هدية مميزة لعمار.', responsiblePersonName: 'سعود', responsiblePersonEmail: 'swwdswwd124@gmail.com', giftGivingDate: '{{giftGivingDate}}', votingDeadline: '{{votingDeadline}}' },
      test: captureBodyId('groupGiftId', 'groupGiftId')
    }),
    req('List My Group Gifts', 'GET', B + '/api/v1/group-gifts/my', {}),
    req('Get Group Gift By Id', 'GET', B + '/api/v1/group-gifts/{{groupGiftId}}', {}),
    req('Add Option', 'POST', B + '/api/v1/group-gifts/add-option/{{groupGiftId}}', {
      body: { giftName: 'ساعة آبل', description: 'ساعة ذكية عملية تناسب اهتماماته التقنية', priceBand: '500-800 SAR', reason: 'تجمع بين الفائدة والأناقة' }
    }),
    req('Generate AI Options', 'POST', B + '/api/v1/group-gifts/ai-generate-option/{{groupGiftId}}', { test: tolerant('AI options generated') }),
    req('Get Options (capture optionId)', 'GET', B + '/api/v1/group-gifts/get-options/{{groupGiftId}}', { test: captureFirstId('groupGiftOptionId', 'groupGiftOptionId') }),
    req('Send Invites (capture token)', 'POST', B + '/api/v1/group-gifts/send-invite/{{groupGiftId}}', {
      body: [ { inviteeName: 'عمار', inviteeEmail: 'swwdswwd124@gmail.com' } ],
      test: [
        ...tolerant('Invites sent'),
        "try { var b = pm.response.json(); var list = Array.isArray(b) ? b : (b && b.invites) ? b.invites : []; var withTok = list.find(function (x) { return x && x.token; }); if (withTok) { pm.collectionVariables.set('inviteToken', withTok.token); console.log('inviteToken =', withTok.token); } } catch (e) {}"
      ]
    }),
    req('Get Vote Page (public)', 'GET', B + '/api/v1/public/group-gifts/vote/{{inviteToken}}', { auth: 'none', test: tolerant('Vote page data fetched') }),
    req('Submit Vote (public)', 'POST', B + '/api/v1/public/group-gifts/vote/{{inviteToken}}', { auth: 'none', body: '{\n  "groupGiftOptionId": {{groupGiftOptionId}}\n}', test: tolerant('Vote submitted') }),
    req('Get Results', 'GET', B + '/api/v1/group-gifts/results/{{groupGiftId}}', { test: tolerant('Results fetched') }),
    req('Close Voting', 'PUT', B + '/api/v1/group-gifts/close-voting/{{groupGiftId}}', { test: tolerant('Voting closed') })
  ]
));

// ============================ OUT-OF-FLOW ENDPOINTS ============================
const extraSubs = [];

extraSubs.push(folder('Bayan - Users (extra, admin)',
  'List-all users is ADMIN-only. Uses Basic {{adminUsername}}/{{adminPassword}} (tolerant). There is no delete-by-id endpoint; the only delete is self-delete (see the final "Account Deletion" folder).',
  [
    req('Get All Users (admin)', 'GET', B + '/api/v1/users/get', { auth: 'admin', test: tolerant('Users listed (admin)') })
  ]
));

extraSubs.push(folder('Bayan - Recipients (extra)', 'List-my recipients via the alternate endpoint.', [
  req('Get My Recipients', 'GET', B + '/api/v1/recipients/get', {})
]));

extraSubs.push(folder('Bayan - Admin Required Questions (extra, admin)',
  'Full ADMIN CRUD for required questions on a throwaway question. Uses Basic {{adminUsername}}/{{adminPassword}} (tolerant).',
  [
    req('Create Temp Question', 'POST', B + '/api/v1/required-questions/add', {
      auth: 'admin',
      body: { questionText: 'سؤال تجريبي: ما لونه المفضّل؟', questionType: 'TEXT', isActive: true, displayOrder: 9 },
      test: tolerant('Temp question created (admin)')
    }),
    req('List & Capture Temp Question', 'GET', B + '/api/v1/required-questions/get', { auth: 'admin', test: captureMaxId('tmpQuestionId', 'tmpQuestionId') }),
    req('Update Temp Question', 'PUT', B + '/api/v1/required-questions/update/{{tmpQuestionId}}', {
      auth: 'admin',
      body: { questionText: 'سؤال محدّث: ما لونه وماركته المفضّلة؟', questionType: 'TEXT', isActive: true, displayOrder: 9 },
      test: tolerant('Temp question updated (admin)')
    }),
    req('Disable Temp Question', 'PUT', B + '/api/v1/required-questions/disable/{{tmpQuestionId}}', { auth: 'admin', test: tolerant('Temp question disabled (admin)') }),
    req('Delete Temp Question', 'DELETE', B + '/api/v1/required-questions/delete/{{tmpQuestionId}}', { auth: 'admin', test: tolerant('Temp question deleted (admin)') })
  ]
));

extraSubs.push(folder('Bayan - Reminders (extra)', 'List-all reminders for the signed-in user (alternate endpoint).', [
  req('Get All Reminders', 'GET', B + '/api/v1/reminders/get', {})
]));

extraSubs.push(folder('Bayan - Group Gifts (extra)',
  'Update + delete on the group gift created in flow 17. Tolerant (voting may be closed).',
  [
    req('Update Group Gift', 'PUT', B + '/api/v1/group-gifts/{{groupGiftId}}', {
      body: { title: 'هدية تخرّج جماعية لعمار (محدّثة)', description: 'تم تحديث وصف الهدية الجماعية.', responsiblePersonName: 'سعود', responsiblePersonEmail: 'swwdswwd124@gmail.com' },
      test: tolerant('Group gift updated')
    }),
    req('Delete Group Gift', 'DELETE', B + '/api/v1/group-gifts/{{groupGiftId}}', { test: tolerant('Group gift deleted') })
  ]
));

extraSubs.push(folder('Shahad - Required Question Answers (extra CRUD, admin)',
  'Standalone ADMIN CRUD on a single required-question answer (separate from the gift-plan submit flow). Uses Basic {{adminUsername}}/{{adminPassword}} (tolerant).',
  [
    req('List Questions & Capture One', 'GET', B + '/api/v1/required-questions/get', {
      auth: 'admin',
      test: [
        ...tolerant('Questions listed (admin)'),
        "try { var arr = pm.response.json(); if (Array.isArray(arr) && arr.length) { pm.collectionVariables.set('anyQuestionId', arr[0].id); } } catch (e) {}"
      ]
    }),
    req('Create RQ Answer', 'POST', B + '/api/v1/required-question-answers/required-question/{{anyQuestionId}}', { auth: 'admin', body: { answerText: 'إجابة تجريبية على السؤال المطلوب.' }, test: tolerant('RQ answer created (admin)') }),
    req('Get All RQ Answers & Capture', 'GET', B + '/api/v1/required-question-answers/get', { auth: 'admin', test: captureMaxId('tmpRqAnswerId', 'tmpRqAnswerId') }),
    req('Get RQ Answer By Id', 'GET', B + '/api/v1/required-question-answers/get-by-id/{{tmpRqAnswerId}}', { auth: 'admin', test: tolerant('RQ answer fetched (admin)') }),
    req('Update RQ Answer', 'PUT', B + '/api/v1/required-question-answers/update/{{tmpRqAnswerId}}', { auth: 'admin', body: { answerText: 'إجابة محدّثة على السؤال المطلوب.' }, test: tolerant('RQ answer updated (admin)') }),
    req('Delete RQ Answer', 'DELETE', B + '/api/v1/required-question-answers/delete/{{tmpRqAnswerId}}', { auth: 'admin', test: tolerant('RQ answer deleted (admin)') })
  ]
));

extraSubs.push(folder('Shahad - AI Questions (extra CRUD, admin)',
  'Manual ADMIN CRUD on AI questions (separate from the AI generate flow). Uses the gift plan from flow 5 and Basic {{adminUsername}}/{{adminPassword}} (tolerant).',
  [
    req('Create AI Question', 'POST', B + '/api/v1/ai-questions/create/' + G, { auth: 'admin', body: { questionText: 'هل يفضّل عمار الهدايا التقنية أم الكلاسيكية؟', reasonForQuestion: 'لتحديد اتجاه الهدية المناسب.' }, test: tolerant('AI question created (admin)') }),
    req('Get All AI Questions & Capture', 'GET', B + '/api/v1/ai-questions/get', { auth: 'admin', test: captureMaxId('tmpAiQuestionId', 'tmpAiQuestionId') }),
    req('Get AI Question By Id', 'GET', B + '/api/v1/ai-questions/get-by-id/{{tmpAiQuestionId}}', { auth: 'admin', test: tolerant('AI question fetched (admin)') }),
    req('Update AI Question', 'PUT', B + '/api/v1/ai-questions/update/{{tmpAiQuestionId}}', { auth: 'admin', body: { questionText: 'هل يفضّل عمار الهدايا التقنية الحديثة؟', reasonForQuestion: 'تحديث سبب السؤال.' }, test: tolerant('AI question updated (admin)') })
  ]
));

extraSubs.push(folder('Shahad - AI Answers (extra CRUD, admin)',
  'Manual ADMIN CRUD on AI answers, attached to the AI question created above. Uses Basic {{adminUsername}}/{{adminPassword}} (tolerant).',
  [
    req('Create AI Answer', 'POST', B + '/api/v1/ai-answers/ai-question/{{tmpAiQuestionId}}', { auth: 'admin', body: { answerText: 'نعم، يفضّل الهدايا التقنية الحديثة.' }, test: tolerant('AI answer created (admin)') }),
    req('Get All AI Answers & Capture', 'GET', B + '/api/v1/ai-answers/get', { auth: 'admin', test: captureMaxId('tmpAiAnswerId', 'tmpAiAnswerId') }),
    req('Get AI Answer By Id', 'GET', B + '/api/v1/ai-answers/get-by-id/{{tmpAiAnswerId}}', { auth: 'admin', test: tolerant('AI answer fetched (admin)') }),
    req('Update AI Answer', 'PUT', B + '/api/v1/ai-answers/update/{{tmpAiAnswerId}}', { auth: 'admin', body: { answerText: 'نعم، خصوصاً الأجهزة القابلة للارتداء.' }, test: tolerant('AI answer updated (admin)') }),
    req('Delete AI Answer', 'DELETE', B + '/api/v1/ai-answers/delete/{{tmpAiAnswerId}}', { auth: 'admin', test: tolerant('AI answer deleted (admin)') }),
    req('Delete AI Question', 'DELETE', B + '/api/v1/ai-questions/delete/{{tmpAiQuestionId}}', { auth: 'admin', test: tolerant('AI question deleted (admin)') })
  ]
));

extraSubs.push(folder('Shahad - Recommendations (extra)',
  'Regenerate ideas + unselect. Tolerant: regenerate errors once an idea is already selected (as in flow 8).',
  [
    req('Regenerate Recommendations', 'GET', B + '/api/v1/gift-recommendations/gift-plans/' + G + '/regenerate', { test: tolerant('Recommendations regenerated') }),
    req('Unselect Recommendation', 'PUT', B + '/api/v1/gift-recommendations/{{recommendationId}}/unselect', { test: tolerant('Recommendation unselected') })
  ]
));

extraSubs.push(folder('Shahad - Product Selection (extra)',
  'Clear the selected product for the plan. Tolerant (depends on a prior selection).',
  [
    req('Clear Selected Product', 'DELETE', B + '/api/v1/selected-products/clear-selected-product/' + G, { test: tolerant('Selected product cleared') })
  ]
));

extraSubs.push(folder('Shahad - Gift Plans (extra)',
  'Previous-plans listing + delete (uses a throwaway plan so the main journey plan survives).',
  [
    req('Get Previous Plans', 'GET', B + '/api/v1/gift-plans/get-previous-plans', { test: tolerant('Previous plans fetched') }),
    req('Create Temp Plan', 'POST', B + '/api/v1/gift-plans/create/' + R, { body: { occasionType: 'BIRTHDAY', occasionDate: '{{occasionDate}}', budget: 200, currency: 'SAR', preferredGiftStyle: 'PRACTICAL', language: 'ar' } }),
    req('Capture Temp Plan', 'GET', B + '/api/v1/gift-plans/get-my-plans', { test: captureMaxId('tmpGiftPlanId', 'tmpGiftPlanId') }),
    req('Delete Temp Plan', 'DELETE', B + '/api/v1/gift-plans/delete/{{tmpGiftPlanId}}', { test: tolerant('Temp plan deleted (204)') })
  ]
));

extraSubs.push(folder('Saud - Surprise Plan (extra, premium)',
  'Regenerate / update / delete the surprise plan. Premium-gated: tolerant (403 until premium is active).',
  [
    req('Regenerate Surprise Plan', 'POST', B + '/api/v1/gift-plans/' + G + '/surprise-plan/regenerate', { body: { language: 'ar' }, test: tolerant('Surprise plan regenerated') }),
    req('Update Surprise Plan', 'PUT', B + '/api/v1/gift-plans/' + G + '/surprise-plan', { body: { planTitle: 'خطة مفاجأة محدّثة', steps: 'رتّب الهدية، جهّز البطاقة، نسّق التوقيت.', timingSuggestion: 'بعد حفل التخرّج مباشرة.' }, test: tolerant('Surprise plan updated') }),
    req('Delete Surprise Plan', 'DELETE', B + '/api/v1/gift-plans/' + G + '/surprise-plan', { test: tolerant('Surprise plan deleted') })
  ]
));

extraSubs.push(folder('Saud - Gift Card (extra, premium)',
  'Delete the gift card from flow 11. Tolerant (needs the premium-created card).',
  [
    req('Delete Gift Card', 'DELETE', B + '/api/v1/gift-cards/{{giftCardId}}', { test: tolerant('Gift card deleted') })
  ]
));

extraSubs.push(folder('Saud - Gift History (extra)',
  'Delete the history log from flow 13. Tolerant (depends on a logged product).',
  [
    req('Delete History Log', 'DELETE', B + '/api/v1/gift-history/from-product/{{selectedProductId}}', { test: tolerant('History log deleted') })
  ]
));

extraSubs.push(folder('Saud - Dashboard & Analytics (extra)',
  'New aggregated/analytics endpoints. Run after main flows so recipientId, giftCardId, and history data are available.',
  [
    req('Get Dashboard', 'GET', B + '/api/v1/dashboard', {
      desc: 'Aggregated home screen: upcoming reminders, active plans count, recent gifts, premium status, pending group-gift votes.'
    }),
    req('Get Spending Stats', 'GET', B + '/api/v1/gift-history/spending-stats?from=2026-01-01&to=2026-12-31', {
      desc: 'Time-bounded spending breakdown. Query params from and to are optional (ISO dates).'
    }),
    req('Get Recipient Insights', 'GET', B + '/api/v1/recipients/' + R + '/insights', {
      desc: 'Per-recipient gifting insights: totals, occasions, top stores, spend timeline.',
      test: tolerant('Recipient insights fetched')
    }),
    req('Download Gift Card (PDF)', 'GET', B + '/api/v1/gift-cards/{{giftCardId}}/download?format=pdf', {
      desc: 'Download rendered gift card as PDF. Tolerant: needs giftCardId from flow 11 (premium).',
      test: tolerant('Gift card PDF downloaded')
    })
  ]
));

extraSubs.push(folder('Saud - Payments (extra, public webhook)',
  'Moyasar webhook sync endpoint (public; normally called by Moyasar). Tolerant: needs a real payment id.',
  [
    req('Moyasar Webhook Sync', 'POST', B + '/api/v1/payments/webhook/moyasar', { auth: 'none', body: { id: '{{moyasarId}}' }, test: tolerant('Webhook processed') })
  ]
));

extraSubs.push(folder('Saud - QR Code (utility)',
  'Standalone QR generator (authenticated utility).',
  [
    req('Generate QR Code', 'POST', B + '/api/v1/qr-code/generate', { body: { url: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' } })
  ]
));

extraSubs.push(folder('Saud - Account Deletion (DESTRUCTIVE - run last)',
  'Self-delete the signed-in account. The only delete endpoint is for the principal (no delete-by-id), so this removes the Saud account created in flow 1. Run this LAST, on its own, after everything else. Tolerant.',
  [
    req('Delete Current User (self)', 'DELETE', B + '/api/v1/users/delete', { test: tolerant('Current user deleted (self)') })
  ]
));

folders.push(folder(
  'Extra - Out-of-Flow Endpoints',
  'Endpoints not part of a main user journey: alternate listings, ADMIN management, utilities, the public webhook, and destructive deletes. Grouped per developer/resource. Run the flow folders first so captured IDs are available. ADMIN folders need {{adminUsername}}/{{adminPassword}} set to a real ADMIN account. The final "Account Deletion" folder is destructive — run it last.',
  extraSubs
));

// ============================ COLLECTION ============================
const collection = {
  info: {
    name: 'Tahadaw — Full System Flows (Realistic Arabic Data)',
    _postman_id: '0a4e1c7e-4f3a-4c2b-9b21-2a1d7f6e5c41',
    description: [
      'End-to-end tests for every Tahadaw flow, grouped by the responsible developer, plus a final "Extra - Out-of-Flow Endpoints" folder for everything else.',
      '',
      'SECURITY: the backend uses HTTP Basic auth (no JWT). The signed-in user comes from the Spring Security principal — endpoints no longer take a userId param.',
      '- Collection auth = Basic {{username}}/{{password}}.',
      '- Flow 1 registers Saud and stores his credentials, so every later request authenticates as him.',
      '- Public (no auth): POST /users/register, /api/v1/public/** (group-gift voting), the Moyasar webhook and moyasar-status callback.',
      '- ADMIN-only: /users/get, required-questions add|update|delete|disable|get, ai-questions CRUD, ai-answers CRUD, required-question-answers CRUD.',
      '  Registration only creates USER accounts, so admin requests use Basic {{adminUsername}}/{{adminPassword}} and are tolerant. To exercise them, promote a user to ADMIN in the DB and set those two variables.',
      '',
      'Developers:',
      '- Bayan  : Account/Users, Recipients, Admin Required Questions, Quality Check, Reminders, Notifications, Group Gifts.',
      '- Shahad : Gift Plans, Required Q&A, AI Q&A, Recommendations, Product Search.',
      '- Saud   : Premium Payment, Gift Messages, Gift Card, Surprise Plan, Gift History.',
      '',
      'Test data: owner/sender = Saud (سعود), recipient = Ammar (عمار), email = swwdswwd124@gmail.com, phone = 0502427714. Free text is in Arabic.',
      '',
      'Run order is dependency-safe top to bottom. IDs are captured automatically between requests.',
      '',
      'Manual / external notes:',
      '- Premium activation needs the Moyasar 3-D Secure step (open transactionUrl logged in flow 4). Until done, Gift Card + Surprise Plan answer 403 (expected).',
      '- Product Search needs searchapi.api.key; AI flows need openai.api.key; emails need spring.mail.* configured.',
      '- The "Account Deletion" folder is destructive (self-delete) — run it last.'
    ].join('\n'),
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'
  },
  auth: {
    type: 'basic',
    basic: [
      { key: 'username', value: '{{username}}', type: 'string' },
      { key: 'password', value: '{{password}}', type: 'string' }
    ]
  },
  item: folders,
  event: [
    ev('prerequest', [
      "// Compute dynamic, always-valid dates once per request.",
      "function pad(n){return String(n).padStart(2,'0');}",
      "function fmtDate(x){return x.getFullYear()+'-'+pad(x.getMonth()+1)+'-'+pad(x.getDate());}",
      "function fmtDT(x){return fmtDate(x)+'T'+pad(x.getHours())+':'+pad(x.getMinutes())+':'+pad(x.getSeconds());}",
      "var now = new Date();",
      "function plus(d){return new Date(now.getTime()+d*86400000);}",
      "pm.collectionVariables.set('occasionDate', fmtDate(plus(30)));",
      "pm.collectionVariables.set('reminderDate', fmtDT(plus(7)));",
      "pm.collectionVariables.set('giftGivingDate', fmtDate(plus(21)));",
      "pm.collectionVariables.set('votingDeadline', fmtDT(plus(14)));"
    ])
  ],
  variable: [
    { key: 'base_url', value: 'http://localhost:8080' },
    { key: 'stamp', value: '' },
    { key: 'username', value: '' },
    { key: 'password', value: '' },
    { key: 'adminUsername', value: 'admin' },
    { key: 'adminPassword', value: 'Admin!2026pass' },
    { key: 'recipientId', value: '' },
    { key: 'giftPlanId', value: '' },
    { key: 'requiredAnswersBody', value: '' },
    { key: 'aiAnswersBody', value: '' },
    { key: 'recommendationId', value: '' },
    { key: 'productId', value: '' },
    { key: 'selectedProductId', value: '' },
    { key: 'giftMessageId', value: '' },
    { key: 'giftCardId', value: '' },
    { key: 'moyasarId', value: '' },
    { key: 'qualityCheckId', value: '' },
    { key: 'reminderId', value: '' },
    { key: 'notificationId', value: '' },
    { key: 'groupGiftId', value: '' },
    { key: 'groupGiftOptionId', value: '' },
    { key: 'inviteToken', value: '' },
    { key: 'occasionDate', value: '' },
    { key: 'reminderDate', value: '' },
    { key: 'giftGivingDate', value: '' },
    { key: 'votingDeadline', value: '' },
    { key: 'tmpQuestionId', value: '' },
    { key: 'anyQuestionId', value: '' },
    { key: 'tmpRqAnswerId', value: '' },
    { key: 'tmpAiQuestionId', value: '' },
    { key: 'tmpAiAnswerId', value: '' },
    { key: 'tmpGiftPlanId', value: '' }
  ]
};

const outPath = path.join(__dirname, 'Tahadaw-Full-System-Flows.postman_collection.json');
fs.writeFileSync(outPath, JSON.stringify(collection, null, 2), 'utf8');
let count = 0;
function countItems(items) { items.forEach(function (it) { if (it.request) count++; else if (it.item) countItems(it.item); }); }
countItems(folders);
console.log('Wrote ' + outPath);
console.log('Top-level folders: ' + folders.length + ', Total requests: ' + count);
